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
