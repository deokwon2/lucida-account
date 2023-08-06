package com.nkia.lucida.account.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.mongodb.TenantWebConfigurerSupport;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebMvc
public class TWebConfig extends TenantWebConfigurerSupport {



  private final ApplicationContext applicationContext;

  @Override
  public String getRequestHeaderName() {
    return null;
  }

  @Override
  public void afterPropertiesSet() {
    // nothing to do..
  }

  @Override
  public void onStartup(ServletContext servletContext) {
    // nothing to do..
  }

  private static final String[] EXCLUDE_PATHS =
      {"/error", "/swagger/account/**", "/api/account/test/**", "/api/account/jwt/**",
          "/api/account/login", "/api/account/pre-login"};

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    JwtTokenService jwtTokenService = applicationContext.getBean(JwtTokenService.class);
    registry.addInterceptor(new TWebInterceptor(jwtTokenService)).addPathPatterns("/**")
        .excludePathPatterns(EXCLUDE_PATHS);
  }


  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowCredentials(true).allowedOriginPatterns("*").allowedMethods("*")
        .allowedHeaders("*").maxAge(86400);
  }
}
