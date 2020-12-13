package com.zkdlu.rest.controller.v2;

import com.zkdlu.rest.advice.exception.EmailSigninFailedException;
import com.zkdlu.rest.advice.exception.UserExistException;
import com.zkdlu.rest.advice.exception.UserNotFoundException;
import com.zkdlu.rest.config.security.JwtTokenProvider;
import com.zkdlu.rest.entity.CommonResult;
import com.zkdlu.rest.entity.SingleResult;
import com.zkdlu.rest.entity.User;
import com.zkdlu.rest.entity.social.KakaoProfile;
import com.zkdlu.rest.repo.UserRepository;
import com.zkdlu.rest.service.KakaoService;
import com.zkdlu.rest.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@Api(tags = {"2. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2")
public class SignController {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;
    private final PasswordEncoder passwordEncoder;
    private final KakaoService kakaoService;

    @ApiOperation(value = "소셜 로그인", notes = "소셜 회원 로그인을 한다.")
    @PostMapping(value = "/signin/{provider}")
    public SingleResult<String> signinByProvider(
            @ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider,
            @ApiParam(value = "서비스 access_token", required = true) @RequestParam String accessToken) {

        KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
        User user = userRepository.findByUidAndProvider(String.valueOf(profile.getId()), provider).orElseThrow(UserNotFoundException::new);
        return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRoles()));
    }

    @ApiOperation(value = "소셜 계정 가입", notes = "소셜 계정 회원가입을 한다.")
    @PostMapping(value = "/signup/{provider}")
    public CommonResult signupProvider(@ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider,
                                       @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken,
                                       @ApiParam(value = "이름", required = true) @RequestParam String name) {

        KakaoProfile profile = kakaoService.getKakaoProfile(accessToken);
        Optional<User> user = userRepository.findByUidAndProvider(String.valueOf(profile.getId()), provider);
        if(user.isPresent())
            throw new UserExistException();

        userRepository.save(User.builder()
                .uid(String.valueOf(profile.getId()))
                .provider(provider)
                .name(name)
                .roles(Collections.singletonList("ROLE_USER"))
                .build());

        return responseService.getSuccessResult();
    }

    @ApiOperation(value = "로그인", notes = "이메일 회원 로그인을 한다.")
    @PostMapping(value = "/signin")
    public SingleResult<String> signin(@ApiParam(value = "회원ID : 이메일", required = true) @RequestParam String id,
                                       @ApiParam(value = "비밀번호", required = true) @RequestParam String password) {
        User user = userRepository.findByUid(id).orElseThrow(EmailSigninFailedException::new);
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new EmailSigninFailedException();

        return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(user.getId()), user.getRoles()));
    }

    @ApiOperation(value = "가입", notes = "회원가입을 한다.")
    @PostMapping(value = "/signup")
    public CommonResult signin(@ApiParam(value = "회원ID : 이메일", required = true) @RequestParam String id,
                               @ApiParam(value = "비밀번호", required = true) @RequestParam String password,
                               @ApiParam(value = "이름", required = true) @RequestParam String name) {

        userRepository.save(User.builder()
                .uid(id)
                .password(passwordEncoder.encode(password))
                .name(name)
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
        return responseService.getSuccessResult();
    }
}