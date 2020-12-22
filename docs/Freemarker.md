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
> suffix만 해줘도 되네

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



### Thymeleaf

1. 의존성 추가

   ```gradle
   dependencies {
       implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
       implementation 'org.springframework.boot:spring-boot-starter-web'
   }
   ```

2. resources/templates 하위에 html 파일 생성

   ```html
   <!DOCTYPE html>
   <html>
   <head>
       <meta charset="UTF-8">
       <title>Thymeleaf 예제</title>
   </head>
   <body>
   <h1>Thymeleaf 예제</h1>
   <span th:text="${user.userId}"></span><br/>
   <span th:text="${user.name}"></span><br/>
   <span th:text="${user.authType}"></span><br/>
   <span th:text="${user.authType}"></span><br/>
   </body>
   </html>
   ```

3. application.yml에서 view resolver 설정

   ```yml
   spring:
     thymeleaf:
       suffix: .html
       cache: false
   ```

   > 배포  할 경우 cache는 true로

4. Controller 설정

   ```java
   @Controller
   public class UserController {
       @GetMapping("/")
       public String getUser(Model model) {
           User user = new User("id", "name", "hello");
           model.addAttribute("user", user);
           return "test";
       }
   }
   ```

   

