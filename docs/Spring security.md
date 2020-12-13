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
