package com.nkia.lucida.account.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nkia.lucida.account.dto.LoginDto;
import com.nkia.lucida.account.dto.UserInfoDto;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.service.AuthenticationService;
import com.nkia.lucida.account.service.RoleManagementService;
import com.nkia.lucida.account.service.UserManagementService;
import com.nkia.lucida.common.auth.AuthToken;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.mongodb.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api")
@Tag(name = "1. Authentication", description = "")
public class AuthenticationController {


  private final AuthenticationService authenticationService;
  private final JwtTokenService jwtTokenService;
  private final UserManagementService userManagementService;
  private final RoleManagementService roleManagementService;



  public AuthenticationController(AuthenticationService authenticationService,
      JwtTokenService jwtTokenService, UserManagementService userManagementService,
      RoleManagementService roleManagementService) {
    this.authenticationService = authenticationService;
    this.jwtTokenService = jwtTokenService;
    this.userManagementService = userManagementService;
    this.roleManagementService = roleManagementService;
  }



  @Operation(summary = "login based on id and password only",
      description = "Set Organization ID to null.")
  @PostMapping(path = "/account/pre-login", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<List<Map<String, String>>> preLogIn(@RequestBody LoginDto logInDto) {
    List<Map<String, String>> response =
        authenticationService.login(logInDto.getLoginId(), logInDto.getPassword());
    return ApiResponseData.createSuccess(response);
  }



  @Operation(summary = "login based on id, password, organizationId", description = "")
  @PostMapping(path = "/account/login", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<AuthToken> logIn(@RequestBody LoginDto logInDto) {
    AuthToken response = authenticationService.login(logInDto.getLoginId(), logInDto.getPassword(),
        logInDto.getOrganizationId());
    return ApiResponseData.createSuccess(response);
  }



  @Operation(summary = "JWT를 갱신할 때 사용되는 API. 반드시 refresh_token을 사용해서 요청해야 한다.", description = "",
      security = {@SecurityRequirement(name = "bearerAuth")})
  @GetMapping(value = "/account/jwt/refresh")
  public ResponseEntity<Object> refreshJWT(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String requestUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String requestOrganizationId =
        jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    User user =
        userManagementService.selectUserById(requestUserId, requestOrganizationId, requestUserId);
    UserInfoDto userInfoDto = new UserInfoDto(user);

    TenantContextHolder.INSTANCE.setTenantId(requestOrganizationId);

    String refreshedToken = jwtTokenService.createRefreshJwtToken(headerAuthorization,
        userInfoDto.getId(), userInfoDto.getLoginId(), requestOrganizationId,
        requestOrganizationId == null ? null
            : roleManagementService.selectRoleByUserIncludeIdFileds(requestUserId),
        userInfoDto.getLocale());

    TenantContextHolder.INSTANCE.clear();
    return ResponseEntity.status(HttpStatus.OK).body(refreshedToken);
  }


  @Operation(summary = "Istio에서 JWKS 정보를 받아올 때 요청하는 API.", description = "")
  // @GetMapping("/jwt/.well-known/jwks.json")
  @GetMapping("/account/jwt/.well-known/jwks.json")
  public Map<String, Object> keys() {
    return jwtTokenService.getJwkSet();
  }
}
