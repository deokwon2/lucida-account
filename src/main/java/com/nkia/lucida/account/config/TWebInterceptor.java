package com.nkia.lucida.account.config;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import com.nkia.lucida.common.auth.AuthConstant;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.exception.AuthErrorCode;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.mongodb.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TWebInterceptor implements HandlerInterceptor {


  private JwtTokenService jwtTokenService;

  public TWebInterceptor(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {

    String organizationId = jwtTokenService
        .getOrganizationIdFromBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));

    if (organizationId == null) {
      organizationId = request.getParameter(AuthConstant.ORGANIZATION_ID);
    }

    if (organizationId != null) {
      TenantContextHolder.INSTANCE.setTenantId(organizationId);
      return true;
    }

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      // https://velog.io/@ojwman/spring-boot-cors-header-preflight
      return true;
    }

    throw new AuthException("Not available token. OrganizationId needs.",
        AuthErrorCode.AUTHENTICATION, null);
  }



  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    TenantContextHolder.INSTANCE.clear();
  }

}
