참고 https://d2.naver.com/helloworld/24942

### OAuth2 
1. [Client] Social 로그인
2. [Social] 로그인 성공 시 authorization_code 반환
3. [Client] authorization_code로 access_token 요청
4. [Social] access_token 반환
5. [Client] access_token으로 서버에 로그인 요청
6. [Server] access_token으로 유저 정보 요청
7. [Social] 유저정보 반환 
8. [Server] 유저 인증 후 JWT 인증 토큰 발금
9. [Client] JWT 인증 토큰으로 api 요청
10. [Server] api 결과 반환

## 1. 카카오 로그인
### 카카오 개발자 센터 설정
1. 카카오 개발자 센터에 app 생성 (https://developers.kakao.com/)
2. 플랫폼 생성(web: http://localhost:8080)
3. Redirect URI 등록 (http://localhost:8080/social/login/kakao)
  > 카카오 로그인 연동 후 인증 성공시 Callback 받을 URL
  
### 로그인 페이지 및 콜백 페이지 연동
### accessToken으로 가입 및 로그인 처리

1. 앱 등록 정보로 카카오 로그인 페이지 띄우기
2. 카카오 로그인을 완료 시 앱 연동 화면이 나오고 동의를 통해 앱과 카카오 계정을 연결
3. 앱에 설정한 콜백 페이지가 인증 코드와 함꼐 호출
4. 전달된 콜백 페이지에서 인증코드로 token을 얻어 화면에 표시

- KaKao와 통신을 위해 RestTemplate Bean추가
```java
@SpringBootApplication
public class RestApplication {
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
```

- 카카오 응답 결과를 객체로 맴핑하기 위해 Gson라이브러리 의존성 추가
```gradle
dependencies {
    implementation 'com.google.code.gson:gson'
}
```

- 카카오 연동을 위한 설정 정보를 application.yml에 추가
```yml
social:
  kakao:
    client_id: eab1c5bb192f3e54dc1e68f6daa281f6
    redirect: /social/login/kakao
    url:
      login: https://kauth.kakao.com/oauth/authorize
      token: https://kauth.kakao.com/oauth/token
      profile: https://kapi.kakao.com/v2/user/me
```

- 로그인, 콜백 처리를 위한 SocialController 생성
```java
@RequiredArgsConstructor
@Controller
@RequestMapping("/social/login")
public class SocialController {

    private final Environment env;
    private final RestTemplate restTemplate;
    private final Gson gson;
    private final KakaoService kakaoService;

    @Value("${spring.url.base}")
    private String baseUrl;

    @Value("${spring.social.kakao.client_id}")
    private String kakaoClientId;

    @Value("${spring.social.kakao.redirect}")
    private String kakaoRedirect;

    /**
     * 카카오 로그인 페이지
     */
    @GetMapping
    public ModelAndView socialLogin(ModelAndView mav) {

        StringBuilder loginUrl = new StringBuilder()
                .append(env.getProperty("spring.social.kakao.url.login"))
                .append("?client_id=").append(kakaoClientId)
                .append("&response_type=code")
                .append("&redirect_uri=").append(baseUrl).append(kakaoRedirect);

        mav.addObject("loginUrl", loginUrl);
        mav.setViewName("social/login");
        return mav;
    }

    /**
     * 카카오 인증 완료 후 리다이렉트 화면
     */
    @GetMapping(value = "/kakao")
    public ModelAndView redirectKakao(ModelAndView mav, @RequestParam String code) {
        mav.addObject("authInfo", kakaoService.getKakaoTokenInfo(code));
        mav.setViewName("social/redirectKakao");
        return mav;
    }
}
```
> Security Config에서 풀어주자.

- 결과 매핑을 위한 모델
```java
@Getter
@Setter
public class RetKaKaoAuth {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private long expires_in;
    private String scope;
}
```

- templates/social 밑에 login.ftl 생성
```ftl
<button onclick="popupKakaoLogin()">KakaoLogin</button>
<script>
    function popupKakaoLogin() {
        window.open('${loginUrl}', 'popupKakaoLogin', 'width=700,height=500,scrollbars=0,toolbar=0,menubar=no')
    }
</script>
```

- User Entity 수정
> 서비스 제공자를 알아야 하므로 provider 필드 추가, 소셜 가입은 암호가 없으므로 password에 null 허용
```java
public class User implements UserDetails {
    @Id // pk
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true, length = 30)
    private String uid;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 100)
    private String password;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 100)
    private String provider;
```

- Repository에 회원정보 조회 추가
```java
Optional<User> findByUidAndProvider(String uid, String provider);
```

- 카카오 유저 정보를 담을 객체 생성
```java
@Getter
@Setter
@ToString
public class KakaoProfile {
    private long id;
    private Properties properties;

    private static class Properties {
        private String nickname;
        private String thumbnail_image;
        private String profile_image;
    }
}
```

- 카카오 api 통신 중 문제가 발생할 경우의 예외 정의
```java
public class SocialException extends RuntimeException {
    public SocialException(String msg, Throwable t) {
        super(msg, t);
    }
    public SocialException(String msg) {
        super(msg);
    }
    public SocialException() {
        super();
    }
}
```
```java
... ExceptionAdvice.java ....

@ExceptionHandler(SocialException.class)
public CommonResult socialLoginException(HttpServletRequest request, SocialException e) {
    return responseService.getFailResult();
}
```

- Kakao 연동을 담당하는 KakaoService생성
```java
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final RestTemplate restTemplate;
    private final Environment env;
    private final Gson gson;

    @Value("${spring.url.base}")
    private String baseUrl;

    @Value("${spring.social.kakao.client_id}")
    private String kakaoClientId;

    @Value("${spring.social.kakao.redirect}")
    private String kakaoRedirect;

    public KakaoProfile getKakaoProfile(String accessToken) {
        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        try {
            // Request profile
            ResponseEntity<String> response = restTemplate.postForEntity(env.getProperty("spring.social.kakao.url.profile"), request, String.class);
            if (response.getStatusCode() == HttpStatus.OK)
                return gson.fromJson(response.getBody(), KakaoProfile.class);
        } catch (Exception e) {
            throw new SocialException();
        }
        throw new SocialException();
    }

    public RetKakaoAuth getKakaoTokenInfo(String code) {
        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Set parameter
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", baseUrl + kakaoRedirect);
        params.add("code", code);
        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(env.getProperty("spring.social.kakao.url.token"), request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return gson.fromJson(response.getBody(), RetKakaoAuth.class);
        }
        return null;
    }
}
```

- SignController에 카카오 회원가입 기능 추가
```java
@ApiOperation(value = "소셜 계정 가입", notes = "소셜 계정 회원가입을 한다.")
@PostMapping(value = "/signup/{provider}")
public CommonResult signupProvider(@ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider, @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken,
                                   @ApiParam(value = "이름", required = true) @RequestParam String name) {

    KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
    Optional<User> user = userRepository.findByUidAndProvider(String.valueOf(profile.getId()), provider);
    if(user.isPresent())
        throw new UserExistException();

    userRepository.save(User.builder()
            .uid(String.valueOf(profile.getId()))
            .provider(provider)
            .name(name)
            .roles(Collections.singletonList("ROLE_USER"))
            .build());

    return responseService.getSuccessResult();
}
```

- 이미 가입했을 경우 발생할 예외 밑 핸들러 추가
```java
public class UserExistException extends RuntimeException {
    public UserExistException(String msg, Throwable t) {
        super(msg, t);
    }
    public UserExistException(String msg) {
        super(msg);
    }
    public UserExistException() {
        super();
    }
}
```
```java
@ExceptionHandler(UserExistException.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public CommonResult communicationException(HttpServletRequest request, UserExistException e) {
    return responseService.getFailResult();
}
```

- SignController에 카카오 로그인 기능 추가
```java
@ApiOperation(value = "소셜 로그인", notes = "소셜 회원 로그인을 한다.")
@PostMapping(value = "/signin/{provider}")
public SingleResult<String> signinByProvider(
        @ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider,
        @ApiParam(value = "서비스 access_token", required = true) @RequestParam String accessToken) {

    KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
    User user = userRepository.findByUidAndProvider(String.valueOf(profile.getId()), provider).orElseThrow(UserNotFoundException::new);
    return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRoles()));
}
```

- Security Config에 접근 가능하도록 설정
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic().disable() // rest api 이므로 기본설정 사용안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트 된다.
        .csrf().disable() // rest api이므로 csrf 보안이 필요없으므로 disable처리.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt token으로 인증하므로 세션은 필요없으므로 생성안함.
        .and()
            .authorizeRequests() // 다음 리퀘스트에 대한 사용권한 체크
                .antMatchers("/*/signin", "/*/signin/**",
                        "/*/signup", "/*/signup/**",
                        "/social/**").permitAll() // 가입 및 인증 주소는 누구나 접근가능
                .antMatchers(HttpMethod.GET, "helloworld/**", "/exception/**").permitAll() // hellowworld로 시작하는 GET요청 리소스는 누구나 접근가능
                .anyRequest().hasRole("USER") // 그외 나머지 요청은 모두 인증된 회원만 접근 가능//
        .and()
            .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
        .and()
            .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        .and()
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // jwt token 필터를 id/password 인증 필터 전에 넣는다
}
```

