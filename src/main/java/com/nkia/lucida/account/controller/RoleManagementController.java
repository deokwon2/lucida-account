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
import com.nkia.lucida.account.dto.RequestParameterDto;
import com.nkia.lucida.account.dto.RoleInfoDto;
import com.nkia.lucida.account.dto.RoleSaveDto;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.service.GroupManagementService;
import com.nkia.lucida.account.service.OrganizationManagementService;
import com.nkia.lucida.account.service.PermissionService;
import com.nkia.lucida.account.service.RoleManagementService;
import com.nkia.lucida.account.service.UserManagementService;
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
@Tag(name = "5. Role", description = "")
public class RoleManagementController {


  private final JwtTokenService jwtTokenService;
  private final UserManagementService userManagementService;
  private final RoleManagementService roleManagementService;
  private final PermissionService permissionService;
  private final GroupManagementService groupManagementService;

  public RoleManagementController(JwtTokenService jwtTokenService,
      OrganizationManagementService organizationManagementService,
      UserManagementService userManagementService, RoleManagementService roleManagementService,
      PermissionService permissionService, GroupManagementService groupManagementService) {

    this.jwtTokenService = jwtTokenService;
    this.userManagementService = userManagementService;
    this.roleManagementService = roleManagementService;
    this.permissionService = permissionService;
    this.groupManagementService = groupManagementService;
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "역할을 생성한다. (admin 계정은 자동으로 포함되어 생성)")
  @PostMapping(path = "/role/insert", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> insertRole(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RoleSaveDto roleSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    roleSaveDto.setId(null);
    Role role = roleManagementService.insertRole(lUserId, lOrganizationId, roleSaveDto.toEntity(),
        roleSaveDto.getUserIds(), roleSaveDto.getPermissionIds(), roleSaveDto.getGroupIds());

    return ApiResponseData.createSuccess(new RoleInfoDto(role));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "역할의 정보를 변경한다.")
  @PostMapping(path = "/role/update", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> updateRole(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RoleSaveDto roleSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Role role = roleManagementService.updateRole(lUserId, lOrganizationId, roleSaveDto.toEntity(),
        roleSaveDto.getUserIds(), roleSaveDto.getPermissionIds(), roleSaveDto.getGroupIds());

    return ApiResponseData.createSuccess(new RoleInfoDto(role));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "역할을 삭제한다.")
  @PostMapping(path = "/role/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> deleteRole(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<List<String>> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    roleManagementService.deleteRole(lUserId, lOrganizationId, false,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(requestParameterDto.getParameter());
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "역할의 목록을 조회한다.")
  @PostMapping(path = "/role/list")
  public ApiResponseData<Object> selectRoleByConditions(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Role> roles = roleManagementService.selectRoleByConditions(lUserId, lOrganizationId, null);

    List<RoleInfoDto> roleInfoDtos = roles.stream().map(o -> {
      return new RoleInfoDto(o);
    }).toList();

    return ApiResponseData.createSuccess(roleInfoDtos);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "선택한 역할의 상세 정보 및 사용자 목록을 조회 한다. (등록된 사용자는 own=true)")
  @PostMapping(path = "/role/detail", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectRoleById(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Role role = roleManagementService.selectRoleById(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    List<User> users = userManagementService.selectUserByRoleWithOwn(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    Set<Permission> permissions = permissionService.selectPermissionByRoleWithOwn(lUserId,
        lOrganizationId, requestParameterDto.getParameter());

    List<Group> groups = groupManagementService.selectGroupByRoleWithOwn(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(new RoleInfoDto(role, users, permissions, groups));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "동일한 조직의 이름이 존재하는 지 확인. (존재=true)")
  @PostMapping(path = "/role/check-duplicate-name", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> checkDuplicateName(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Boolean duplicate = roleManagementService.checkDuplicateName(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(duplicate);
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "역할의 목록을 조회한다.")
  @PostMapping(path = "/role/list-page-filter")
  public ApiResponseData<Object> selectRoleByConditions(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<Role> pageItems = roleManagementService.selectRoleByConditions(lUserId, lOrganizationId,
        criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = pageItems.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<RoleInfoDto> roleInfoDtos = pageItems.map(u -> {
      RoleInfoDto dto = new RoleInfoDto(u);
      dto.setIndex(index.incrementAndGet());
      return dto;
    });
    return ApiResponseData.createSuccess(roleInfoDtos);
  }
}
