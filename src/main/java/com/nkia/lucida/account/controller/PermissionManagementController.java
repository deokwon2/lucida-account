package com.nkia.lucida.account.controller;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nkia.lucida.account.dto.PermissionInfoDto;
import com.nkia.lucida.account.dto.RequestParameterDto;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.service.PermissionService;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.mongodb.CriteriaMakeHelper;
import com.nkia.lucida.common.mongodb.GridFiltersPageableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/account")
@Tag(name = "4. Permission", description = "")
public class PermissionManagementController {


  private final JwtTokenService jwtTokenService;
  private final PermissionService permissionService;

  public PermissionManagementController(JwtTokenService jwtTokenService,
      PermissionService permissionService) {

    this.jwtTokenService = jwtTokenService;
    this.permissionService = permissionService;
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "로그인 사용자의 토큰을 기준으로 권한 목록을 조회한다.")
  @PostMapping(path = "/permission/list-by-token")
  public ApiResponseData<Object> selectPermissionByToken(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {
    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);
    List<String> lRoleIds = jwtTokenService.getRoleIdsFromBearerToken(headerAuthorization);
    Set<String> permissionIds =
        permissionService.selectPermissionByRoleIncludeIdFields(lUserId, lOrganizationId, lRoleIds);
    return ApiResponseData.createSuccess(permissionIds);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "적용할 수 있는 모든 권한 목록을 조회한다.")
  @PostMapping(path = "/permission/list-all-own")
  public ApiResponseData<Object> selectPermissionAll(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    Set<Permission> permissions =
        permissionService.selectPermissionAllWithOwn(lUserId, lOrganizationId);

    List<PermissionInfoDto> permissionInfoDtos = permissions.stream().map(p -> {
      PermissionInfoDto permissionInfoDto = new PermissionInfoDto();
      return permissionInfoDto.toDto(p);
    }).toList();
    return ApiResponseData.createSuccess(permissionInfoDtos);
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "역할에 따른 권한 목록을 조회한다.")
  @PostMapping(path = "/permission/list-by-role", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectPermissionByByRoleWithOwn(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    Set<Permission> permissions = permissionService.selectPermissionByRoleWithOwn(lUserId,
        lOrganizationId, requestParameterDto.getParameter());

    List<PermissionInfoDto> permissionInfoDtos = permissions.stream().map(p -> {
      PermissionInfoDto permissionInfoDto = new PermissionInfoDto();
      return permissionInfoDto.toDto(p);
    }).toList();
    return ApiResponseData.createSuccess(permissionInfoDtos);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "적용할 수 있는 모든 권한 목록을 조회한다.")
  @PostMapping(path = "/permission/list-all-own-filter")
  public ApiResponseData<Object> selectPermissionAll(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();


    Page<Permission> items = permissionService.selectPermissionAllWithOwn(lUserId, lOrganizationId,
        criterias, gridFiltersPageableDto.toPageable());


    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<PermissionInfoDto> dtoPage = items.map(u -> {
      PermissionInfoDto dto = new PermissionInfoDto();
      dto.setIndex(index.incrementAndGet());
      return dto.toDto(u);
    });

    return ApiResponseData.createSuccess(dtoPage);
  }
}
