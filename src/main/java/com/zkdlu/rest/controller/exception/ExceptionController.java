package com.zkdlu.rest.controller.exception;

import com.zkdlu.rest.advice.exception.AuthenticationEntryPointException;
import com.zkdlu.rest.entity.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/exception")
public class ExceptionController {

    @GetMapping(value = "/entrypoint")
    public CommonResult entrypointException() {
        throw new AuthenticationEntryPointException();
    }

    @GetMapping(value = "accessdenied")
    public CommonResult accessDeniedException() {
        throw new AccessDeniedException("");
    }
}