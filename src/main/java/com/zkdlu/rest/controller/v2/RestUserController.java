package com.zkdlu.rest.controller.v2;

import com.zkdlu.rest.entity.CommonResult;
import com.zkdlu.rest.entity.ListResult;
import com.zkdlu.rest.entity.SingleResult;
import com.zkdlu.rest.entity.User;
import com.zkdlu.rest.repo.UserRepository;
import com.zkdlu.rest.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

