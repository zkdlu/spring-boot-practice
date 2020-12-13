### Freemarker

1. 의존성 추가
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-freemarker'
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```
2. resources/templates 하위에 ftl 파일 생성
```ftl
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="utf-8">
    <title>welcome</title>
</head>
<body>
<h1>This is freemarker sample.</h1>
<h2>${message}</h2>
</body>
</html>
```
3. application.yml에서 view resolver 설정
```yml
spring:
  freemarker:
    template-loader-path: classpath:/templates
    suffix: .ftl
```
4. Controller 설정
```java
@Controller
public class HelloController {
@GetMapping(value = "/helloworld/page")
    public String helloworld(Map model) {
        model.put("message", "hello world");

        return "ftl 파일명";
    }
}
```

### H2 + JPA

1. 의존성 추가
```gradle
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
  runtimeOnly 'com.h2database:h2'
}
```
2. application.yml에 datasource 설정
```yml
spring:
  datasource:
    url: jdbc:h2:mem:test-db
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    database: h2
    show-sql: true
    hibernate:
      ddl-auto: update
```
3. Entity class 작성
```java
@Builder
@Getter
@Entity // jpa entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user") // user 테이블에 매핑
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(nullable = false, unique = true, length = 30)
    private String uid;
    @Column(nullable = false, length = 100)
    private String name;
}
```
4. Controller 작성
```java
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping(value = "/user")
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    @PostMapping(value = "/user")
    public User save() {
        User user = User.builder()
                .uid("zkdlu")
                .name("건")
                .build();

        return userRepository.save(user);
    }
}
```

### Swagger 문서
1. 의존성 추가
```gradle
dependencies {
    compile 'io.springfox:springfox-swagger-ui:2.9.2'
    compile 'io.springfox:springfox-swagger2:2.9.2'
}
```
2. Configuration 작성
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(swaggerInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zkdlu.rest"))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false); // 기본으로 세팅되는 200,401,403,404 메시지를 표시 하지 않음
    }

    private ApiInfo swaggerInfo() {
        return new ApiInfoBuilder().title("REST API Documentation")
                .description("Swaager Document")
                .license("zkdlu")
                .licenseUrl("localhost")
                .version("1")
                .build();
    }
}
```
> - basePackage 하단의 Controller를 문서화 한다.
> - PathSelectors.ant("/v1/**") 로 v1로 시작하는 것만 문서화 시킬 수 있다.
3. Controller 수정
```java
@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    private final UserRepository userRepository;

    @ApiOperation(value = "회원 조회", notes = "전체 회원 조회")
    @GetMapping(value = "/user")
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    @ApiOperation(value = "회원 입력", notes = "회원을 입력")
    @PostMapping(value = "/user")
    public User save(@ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
                     @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .uid(uid)
                .name(name)
                .build();
        return userRepository.save(user);
    }
}
```
> - @Api - Swagger 문서 타이틀
> - @ApiOperation
> - @ApiParam
> - http://localhost:8080/swagger-ui.htm

### Rest api
- 결과 데이터는 기존의 결과 데이터 + api 요청결과로 구성한다.
1. 공통 모델 작성
```java
@Getter
@Setter
public class CommonResult {
    @ApiModelProperty(value = "응답 성공여부 : true/false")
    private boolean success;

    @ApiModelProperty(value = "응답 코드 번호 : 비정상 < 0 <= 정상")
    private int code;

    @ApiModelProperty(value = "응답 메시지")
    private String msg;
}
```
2. 단일 응답을 담는 모델
```java
@Getter
@Setter
public class SingleResult<T> extends CommonResult{
    private T data;
}
```
3. 리스트 응답을 담는 모델
```java
@Getter
@Setter
public class ListResult<T> extends CommonResult {
    private List<T> list;
}
```
