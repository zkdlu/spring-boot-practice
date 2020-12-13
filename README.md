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
