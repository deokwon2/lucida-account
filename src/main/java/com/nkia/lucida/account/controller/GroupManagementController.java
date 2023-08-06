package com.nkia.lucida.account.controller;

import java.util.List;
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
import com.nkia.lucida.account.dto.GroupInfoDto;
import com.nkia.lucida.account.dto.GroupSaveDto;
import com.nkia.lucida.account.dto.RequestParameterDto;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.service.GroupManagementService;
import com.nkia.lucida.account.service.OrganizationManagementService;
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
@Tag(name = "6. Group", description = "")
public class GroupManagementController {


  private final JwtTokenService jwtTokenService;
  private final UserManagementService userManagementService;
  private final GroupManagementService groupManagementService;
  private final RoleManagementService roleManagementService;

  public GroupManagementController(JwtTokenService jwtTokenService,
      OrganizationManagementService organizationManagementService,
      UserManagementService userManagementService, GroupManagementService groupManagementService,
      RoleManagementService roleManagementService) {
    this.jwtTokenService = jwtTokenService;
    this.userManagementService = userManagementService;
    this.groupManagementService = groupManagementService;
    this.roleManagementService = roleManagementService;
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "그룹을 생성한다. (admin 계정은 자동으로 포함되어 생성)")
  @PostMapping(path = "/group/insert", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> insertGroup(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GroupSaveDto groupSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    groupSaveDto.setId(null);
    Group group = groupManagementService.insertGroup(lUserId, lOrganizationId,
        groupSaveDto.toEntity(), groupSaveDto.getUserIds(), groupSaveDto.getRoleIds());

    return ApiResponseData.createSuccess(new GroupInfoDto(group));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "그룹의 정보를 변경한다.")
  @PostMapping(path = "/group/update", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> updateGroup(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GroupSaveDto groupSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Group group = groupManagementService.updateGroup(lUserId, lOrganizationId,
        groupSaveDto.toEntity(), groupSaveDto.getUserIds(), groupSaveDto.getRoleIds());

    return ApiResponseData.createSuccess(new GroupInfoDto(group));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "그룹을 삭제한다.")
  @PostMapping(path = "/group/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> deleteGroup(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<List<String>> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    groupManagementService.deleteGroup(lUserId, lOrganizationId, false,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(requestParameterDto.getParameter());
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "그룹의 목록을 조회한다.")
  @PostMapping(path = "/group/list")
  public ApiResponseData<Object> selectGroupByConditions(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Group> groups =
        groupManagementService.selectGroupByConditions(lUserId, lOrganizationId, null);

    List<GroupInfoDto> groupInfoDtos = groups.stream().map(o -> {

      List<User> users =
          userManagementService.selectUserByGroup(lUserId, lOrganizationId, o.getId());

      return new GroupInfoDto(o, users);
    }).toList();

    return ApiResponseData.createSuccess(groupInfoDtos);
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/group/list-all-own")
  public ApiResponseData<Object> selectGroupForSimple(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Group> groups = groupManagementService.selectGroupAllWithOwn(lUserId, lOrganizationId);

    List<GroupInfoDto> groupInfoDtos = groups.stream().map(o -> {
      return new GroupInfoDto(o);
    }).toList();

    return ApiResponseData.createSuccess(groupInfoDtos);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "선택한 그룹의 상세 정보 및 사용자 목록을 조회 한다. (등록된 사용자는 own=true)")
  @PostMapping(path = "/group/detail", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectGroupById(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Group group = groupManagementService.selectGroupById(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    List<User> users = userManagementService.selectUserByGroupWithOwn(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    List<Role> roles =
        roleManagementService.selectRoleByGroupWithOwn(lUserId, lOrganizationId, group);

    return ApiResponseData.createSuccess(new GroupInfoDto(group, users, roles, true));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "동일한 그룹의 이름이 존재하는 지 확인. (존재=true)")
  @PostMapping(path = "/group/check-duplicate-name", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> checkDuplicateName(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Boolean duplicate = groupManagementService.checkDuplicateName(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(duplicate);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "그룹의 목록을 조회한다.")
  @PostMapping(path = "/group/list-page-filter")
  public ApiResponseData<Object> selectGroupByConditions(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();


    Page<Group> items = groupManagementService.selectGroupByConditions(lUserId, lOrganizationId,
        criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<GroupInfoDto> pageItems = items.map(u -> {
      GroupInfoDto groupInfoDto = new GroupInfoDto(u);
      groupInfoDto.setIndex(index.incrementAndGet());
      return groupInfoDto;
    });
    return ApiResponseData.createSuccess(pageItems);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/group/list-all-own-filter")
  public ApiResponseData<Object> selectGroupForSimple(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();


    Page<Group> items = groupManagementService.selectGroupAllWithOwn(lUserId, lOrganizationId,
        criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<GroupInfoDto> pageItems = items.map(u -> {
      GroupInfoDto groupInfoDto = new GroupInfoDto(u);
      groupInfoDto.setIndex(index.incrementAndGet());
      return groupInfoDto;
    });
    return ApiResponseData.createSuccess(pageItems);
  }
}
