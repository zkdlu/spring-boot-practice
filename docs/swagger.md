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
