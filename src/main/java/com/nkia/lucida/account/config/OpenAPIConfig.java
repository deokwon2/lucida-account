package com.nkia.lucida.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;


@Component
public class OpenAPIConfig {

  @Bean
  public OpenAPI openAPI(@Value("${spring.application.name}") String applicationName,
      @Value("${springdoc.version:v1}") String appVersion,
      @Value("${spring.profiles.active}") String active) {

    Info info = new Info().title(applicationName + " (" + active + ")").version(appVersion)
        .termsOfService("http://www.nkia.co.kr/m21.php?cate=1")
        .license(new License().name("Nkia License").url("http://www.nkia.co.kr/m21.php?cate=1"));

    SecurityScheme securityScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP)
        .scheme("bearer").bearerFormat("JWT").in(SecurityScheme.In.HEADER).name("Authorization");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("bearerAuth", securityScheme)).info(info);
  }

}
