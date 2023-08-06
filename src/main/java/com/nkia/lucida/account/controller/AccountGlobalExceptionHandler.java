package com.nkia.lucida.account.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RestController
public class AccountGlobalExceptionHandler extends GlobalExceptionHandler
    implements ErrorController {

  @ExceptionHandler(AuthException.class)
  public final ApiResponseData<Object> handleException(WebRequest request, AuthException e) {
    log.error("", e);
    return ApiResponseData.createFail(e.getErrorCode(), e.getErrorMsgArgs(), e.getItem());
  }

}
