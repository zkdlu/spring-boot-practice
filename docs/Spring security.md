## Spring security
- Spring security는 DispatcherServlet 앞단에 Filter를 등록해 요청을 가로챔.
- 클라이언트에게 리소스 접근 권한이 없을 경우엔 인증화면으로 리다이렉트 함

## Filter
- Spring security는 기능별 필터의 집합인 필터체인을 가지고 있다.
- 필터의 처리 순서가 중요
- 클라이언트에 리소스 접근 권한이 없을 경우 인증화면으로 리다이렉트 하는 필터는 **UsernamePasswordAuthenticationFilter**
- Rest api의 경우 따로 로그인 폼이 없기 때문에 인증 권한이 없는 오류 Json을 **UsernamePasswordAuthenticationFilter** 전에 처리하여야 함

### Filter chain
1. ChannelProcessingFilter
2. SecurityContextPersistenceFilter
3. ConcurrentSessionFilter
4. HeaderWriterFilter
5. CsrfFilter
6. LogoutFilter
7. X509AuthenticationFilter
8. AbstractPreAuthenticatedProcessingFilter
9. CasAuthenticationFilter
10. UsernamePasswordAuthenticationFilter
11. BasicAuthenticationFilter
12. SecurityContextHolderAwareRequestFilter
13. JaasApiIntegrationFilter
14. RememberMeAuthenticationFilter
15. AnonymousAuthenticationFilter
16. SessionManagementFilter
17. ExceptionTranslationFilter
18. FilterSecurityInterceptor
19. SwitchUserFilter

## API 인증 및 권한 부여, 제한된 리소스의 요청
- 인증을 위해 가입(Sign up), 로그인(Sign in) api 구현
- 가입 시 제한된 리소스에 접근할 수 있는 ROLE_USER 권한을 회원에게 부여
- Spring security 설정에는 접근 제한이 필요한 리소스에 대해서 ROLE_USER 권한을 가져야 접근 가능하도록 세팅
- 권한을 가진 회원이 로그인 성공 시엔 리소스에 접근할 수 있는 Jwt 보안 토큰을 발급
- Jwt 보안 토큰으로 회원은 권한이 필요한 api 리소스를 요청하여 사용

## JWT (Json Web Token)
- Json 객체를 암호화하여 만든 String값으로 기본적으로 암호화되어 있어 변조하기가 어려움
- 다른 토큰과 달리 토큰 자체에 데이터를 가지고 있음.
1. api 서버에서는 로그인이 완료된 클라이언트에게 회원을 구분할 수 있는 값을 넣은 Jwt 토큰을 생성하여 발급
2. 클라이언트는 이 Jwt토큰을 이용하여 권한이 필요한 리소스를 서버에 요청하는데 사용
3. api 서버는 클라이언트에게 전달받은 Jwt 토큰이 유효한지 확인하고 담겨있는 회원정보를 확인
 
 ### 인증 및 권한 부여
 1. 의존성 추가
 ```gradle
 dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt:0.9.1'
}
 ```
 2. JwtTokenProvider 생성
 
Jwt 토큰 생성 및 유효성 검증을 하는 컴포넌트
```java
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    @Value("spring.jwt.secret")
    private String secretKey;

    private long tokenValidMillisecond = 1000L * 60 * 60;

    private final UserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // Jwt 토큰 생성
    public String createToken(String userPk, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now) // 토큰 발행일자
                .setExpiration(new Date(now.getTime() + tokenValidMillisecond)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();
    }

    // Jwt 토큰으로 인증 정보를 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Jwt 토큰에서 회원 구별 정보 추출
    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    // Request의 Header에서 token 파싱 : "X-AUTH-TOKEN: jwt토큰"
    public String resolveToken(HttpServletRequest req) {
        return req.getHeader("X-AUTH-TOKEN");
    }

    // Jwt 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
```
```yml
spring:
  jwt:
    secret: govlepel@$&
```
3. JwtAuthenticationFilter 생성

Jwt 가 유효한 토큰인지 인증하기 위한 Filter
```java
public class JwtAuthenticationFilter extends GenericFilterBean {
    private JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Request로 들어오는 Jwt Token의 유효성을 검증(jwtTokenProvider.validateToken)하는 filter를 filterChain에 등록합니다.
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
```
4. SpringSecurity Config 생성

서버에 보안 설정을 적용

리소스 접근 제한 표현식
- hasIpAddress(ip) : 접근자의 IP주소가 매칭하는지 확인
- hasRole(role) : 역할이 부여된 권한과 일치하는지 확인
- hasAnyRole(role) : 부여된 역할 중 일치하는 항목이 있는지 확인
- perminAll : 모든 접근 승인
- denyAll - 모든 접근 거부
- anonymouse - 익명사용자인이 확인
- authenticated - 인증된 사용자인지 확인
- rememberMe - 사용자가 remember me를 사용해 인증했는지 확인
- fullyAuthenticated - 사용자가 모든 credential을 갖춘 상태에서 인증했는지 확인

```java
@RequiredArgsConstructor
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable() // rest api 이므로 기본설정 사용안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트 된다.
            .csrf().disable() // rest api이므로 csrf 보안이 필요없으므로 disable처리.
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt token으로 인증하므로 세션은 필요없으므로 생성안함.
            .and()
                .authorizeRequests() // 다음 리퀘스트에 대한 사용권한 체크
                    .antMatchers("/*/signin", "/*/signup").permitAll() // 가입 및 인증 주소는 누구나 접근가능
                    .antMatchers(HttpMethod.GET, "helloworld/**").permitAll() // hellowworld로 시작하는 GET요청 리소스는 누구나 접근가능
                    .anyRequest().hasRole("USER") // 그외 나머지 요청은 모두 인증된 회원만 접근 가능
            .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // jwt token 필터를 id/password 인증 필터 전에 넣는다
    }

    @Override // ignore check swagger resource
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs", "/swagger-resources/**",
                "/swagger-ui.html", "/webjars/**", "/swagger/**");
    }
}
```
5. CustomUserDetailService 정의

토큰에 세팅된 유저 정보로 회원정보를 조회하는 UserDetailsService를 재정의
```java
@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String userPk) {
        return userRepository.findById(Long.valueOf(userPk)).orElseThrow(UserNotFoundException::new);
    }
}
```

6. User 수정

Spring security의 보안을 적용하기 위해 User entity에 UserDetails class를 상속받아 추가 정보를 재정의

roles는 회원이 가지고 있는 권한이고 기본 "ROLE_USER"가 세팅되며 여러개가 세팅 될 수 있음.

- getUsername : security에서 사용하는 회원 구분 id
- isAccountNonExpired : 계정이 만료가 안되었는지
- isAccountNonLocked : 계정이 잠기지 않았는지
- isCredentialsNonExpired : 계정 패스워드가 만료 안되었는지
- isEnagled : 계정이 사용 가능한지
> Json 결과로 출력 안할 때에는 @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) 어노테이션 
```java
@Builder
@Getter
@Entity // jpa entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user") // user 테이블에 매핑
public class User implements UserDetails {
    @Id // pk
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true, length = 30)
    private String uid;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 100)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public String getUsername() {
        return this.uid;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

7. UserRepository 수정
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUid(String email);
}
```

8. 로그인 예외 추가
```java
public class EmailSigninFailedException extends RuntimeException {
    public EmailSigninFailedException(String msg, Throwable t) {
        super(msg, t);
    }

    public EmailSigninFailedException(String msg) {
        super(msg);
    }

    public EmailSigninFailedException() {
        super();
    }
}
```
