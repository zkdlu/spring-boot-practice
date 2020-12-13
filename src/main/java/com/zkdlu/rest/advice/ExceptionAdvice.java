package com.zkdlu.rest.advice;

import com.zkdlu.rest.advice.exception.*;
import com.zkdlu.rest.entity.CommonResult;
import com.zkdlu.rest.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {
    private final ResponseService responseService;

    @ExceptionHandler(UserExistException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult communicationException(HttpServletRequest request, UserExistException e) {
        return responseService.getFailResult();
    }

    @ExceptionHandler(SocialException.class)
    public CommonResult socialLoginException(HttpServletRequest request, SocialException e) {
        return responseService.getFailResult();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult AccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        return responseService.getFailResult();
    }

    @ExceptionHandler(AuthenticationEntryPointException.class)
    public CommonResult authenticationEntryPointException(HttpServletRequest request, AuthenticationEntryPointException e) {
        return responseService.getFailResult();
    }

    @ExceptionHandler(EmailSigninFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult emailSigninFailed(HttpServletRequest request, EmailSigninFailedException e) {
        return responseService.getFailResult();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult userNotFoundException(HttpServletRequest request, Exception e) {
        return responseService.getFailResult();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult defaultException(HttpServletRequest request, Exception e) {
        return responseService.getFailResult();
    }
}


