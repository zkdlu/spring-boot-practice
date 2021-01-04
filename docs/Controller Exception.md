### @ControllerAdvice 어노테이션 
- Controller 전역에 적용되는 코드를 작성하게 해줌. (범위 조절 가능)

1. Exception Advice 추가
```java
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {
    private final ResponseService responseService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult defaultException(HttpServletRequest request, Exception e) {
        return responseService.getFailResult();
    }
}
```
> - @RestControllerAdvice(basePackages = "com.zkdlu.controller") : 예외 발생 시 json 형태로 반환
> - @ExceptionHandler(Exception.class) : Exceptino이 발생하면 해당 Handler로 처리
> - @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) : Exceptino이 발생하면 Response에 Http code를 500으로 설정

2. Controller 수정
```java
@ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
@GetMapping(value = "/user/{id}")
public SingleResult<User> findUserById(@ApiParam(value = "회원ID", required = true) @PathVariable long id) throws Exception{
    return responseService.getSingleResult(userRepository.findById(id).orElseThrow(Exception::new));
}
```
> 지정한 범위에서 Exception이 발생하면 ExceptionAdvice에 정의한 Exception Handler가 호출 된다.

### 사용자 정의 예외
1. Exception 클래스 정의
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserNotFoundException(String msg) {
        super(msg);
    }

    public UserNotFoundException() {
        super();
    }
}
```
2. Controller 수정
```java
@ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
@GetMapping(value = "/user/{id}")
public SingleResult<User> findUserById(@ApiParam(value = "회원ID", required = true) @PathVariable long id) {
    return responseService.getSingleResult(userRepository.findById(id).orElseThrow(UserNotFoundException::new));
}
```
