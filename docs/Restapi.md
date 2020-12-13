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
4. 결과모델에 데이터를 담는 서비스 
```java
@Service
public class ResponseService {
    public enum CommonResponse {
        SUCCESS(0, "성공하였습니다."),
        FAIL(-1, "실패하였습니다.");

        int code;
        String msg;

        CommonResponse(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    public <T> ListResult<T> getListResult(List<T> list) {
        ListResult<T> result = new ListResult<>();
        result.setList(list);
        setSuccessResult(result);
        return result;
    }

    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }

    private void getFailResult(CommonResult result) {
        result.setSuccess(false);
        result.setCode(CommonResponse.FAIL.getCode());
        result.setMsg(CommonResponse.FAIL.getMsg());
    }

    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMsg(CommonResponse.SUCCESS.getMsg());
    }
}
```
5. Controller 수정
```java
@Api(tags = {"2. User Rest"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2")
public class RestUserController {
    private final UserRepository userRepository;
    private  final ResponseService responseService;

    @ApiOperation(value = "회원 조회", notes = "전체 회원 조회")
    @GetMapping(value = "/user")
    public ListResult<User> findAllUser() {
        return responseService.getListResult(userRepository.findAll());
    }

    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
    @GetMapping(value = "/user/{id}")
    public SingleResult<User> findUserById(@ApiParam(value = "회원ID", required = true) @PathVariable long id) {
        return responseService.getSingleResult(userRepository.findById(id).orElse(null));
    }

    @ApiOperation(value = "회원 입력", notes = "회원을 입력")
    @PostMapping(value = "/user")
    public SingleResult<User> save(@ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
                                  @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .uid(uid)
                .name(name)
                .build();
        return responseService.getSingleResult(userRepository.save(user));
    }

    @ApiOperation(value = "회원 수정", notes = "회원정보를 수정한다")
    @PutMapping(value = "/user")
    public SingleResult<User> modify(
            @ApiParam(value = "회원번호", required = true) @RequestParam long msrl,
            @ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
            @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .id(msrl)
                .uid(uid)
                .name(name)
                .build();
        return responseService.getSingleResult(userRepository.save(user));
    }

    @ApiOperation(value = "회원 삭제", notes = "userId로 회원정보를 삭제한다")
    @DeleteMapping(value = "/user/{id}")
    public CommonResult delete(@ApiParam(value = "회원번호", required = true) @PathVariable long id) {
        userRepository.deleteById(id);
        return responseService.getSuccessResult();
    }
}
```
