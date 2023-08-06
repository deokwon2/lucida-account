package com.nkia.lucida.account.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nkia.lucida.account.dto.PasswordDto;
import com.nkia.lucida.account.dto.RequestParameterDto;
import com.nkia.lucida.account.dto.UserInfoDto;
import com.nkia.lucida.account.dto.UserInfoPageableDto;
import com.nkia.lucida.account.dto.UserSaveDto;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.service.GroupManagementService;
import com.nkia.lucida.account.service.RoleManagementService;
import com.nkia.lucida.account.service.UserManagementService;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalErrorCode;
import com.nkia.lucida.common.mongodb.CriteriaMakeHelper;
import com.nkia.lucida.common.mongodb.GridFiltersPageableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/account")
@Tag(name = "2. User", description = "")
public class UserManagementController {

  private final UserManagementService userManagementService;
  private final RoleManagementService roleManagementService;
  private final GroupManagementService groupManagementService;
  private final JwtTokenService jwtTokenService;

  public UserManagementController(UserManagementService userManagementService,
      RoleManagementService roleManagementService, GroupManagementService groupManagementService,
      JwtTokenService jwtTokenService) {
    super();
    this.userManagementService = userManagementService;
    this.roleManagementService = roleManagementService;
    this.groupManagementService = groupManagementService;
    this.jwtTokenService = jwtTokenService;
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "사용자를 삭제",
      description = "parameter = ['사용자ID1', '사용자ID2' ...]")
  @PostMapping(path = "/user/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<List<String>> deleteUser(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<List<String>> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    userManagementService.deleteUser(lUserId, lOrganizationId, false,
        requestParameterDto.getParameter());
    return ApiResponseData.createSuccess(requestParameterDto.getParameter());
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      description = "id 필드는 제외 (요청되어도 null 처리됨) ")
  @PostMapping(path = "/user/insert", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> insertUser(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody UserSaveDto userSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    userSaveDto.setId(null);
    User user = userManagementService.insertUser(lUserId, lOrganizationId, userSaveDto.toEntity(),
        userSaveDto.getRoleIds(), userSaveDto.getGroupIds());
    return ApiResponseData.createSuccess(new UserInfoDto(user));
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      description = "password 필드는 제외 (요청되어도 null 처리됨) ")
  @PostMapping(path = "/user/update", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> updateUser(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody UserSaveDto userSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    userSaveDto.setPassword(null);
    User user = userManagementService.updateUser(lUserId, lOrganizationId, userSaveDto.toEntity(),
        userSaveDto.getRoleIds(), userSaveDto.getGroupIds());

    return ApiResponseData.createSuccess(new UserInfoDto(user));
  }

  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "user/list-page", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByConditions(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody UserInfoPageableDto userPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Page<User> items = userManagementService.selectUserByConditions(lUserId, lOrganizationId, true,
        lOrganizationId, userPageableDto.toEntity(), userPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<UserInfoDto> usersDtoPage = items.map(u -> {
      UserInfoDto userInfoDto = new UserInfoDto(u);
      userInfoDto.setIndex(index.incrementAndGet());
      return userInfoDto;
    });
    return ApiResponseData.createSuccess(usersDtoPage);

  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/detail", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<UserInfoDto> selectUserById(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Assert.notNull(requestParameterDto.getParameter(), "id must not be null.");

    User user = userManagementService.selectUserById(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    List<Role> roles =
        roleManagementService.selectRoleByUserWithOwn(lUserId, lOrganizationId, user);

    List<Group> groups =
        groupManagementService.selectGroupByUserWithOwn(lUserId, lOrganizationId, user.getId());

    return ApiResponseData.createSuccess(new UserInfoDto(user, roles, groups));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/update-password-by-admin", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<String> updateUserPasswordByAdmin(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody PasswordDto passwordDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    if (!"admin".equals(lUserId)) {
      throw new AuthException("Only the admin can be changed.", GlobalErrorCode.CAN_NOT_BE_SAVED,
          null);
    }

    passwordDto.setCurrentPassword(null);
    userManagementService.updateUserPasswordByAdmin(lUserId, lOrganizationId, passwordDto.getId(),
        passwordDto.getNewPassword());

    return ApiResponseData.createSuccess(passwordDto.getLoginId());
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/update-password", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<String> updateUserPassword(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody PasswordDto passwordDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    userManagementService.updateUserPassword(lUserId, lOrganizationId, passwordDto.getId(),
        passwordDto.getCurrentPassword(), passwordDto.getNewPassword());

    return ApiResponseData.createSuccess(passwordDto.getLoginId());
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      description = "parameter = 조직ID")
  @PostMapping(path = "/user/list-by-organization", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByOrganizationWithOwn(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<User> users = userManagementService.selectUserByOrganizationWithOwn(lUserId,
        lOrganizationId, requestParameterDto.getParameter());

    List<UserInfoDto> userInfoDtos = users.stream().map(u -> {
      return new UserInfoDto(u);
    }).toList();

    return ApiResponseData.createSuccess(userInfoDtos);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/list-all-own", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserAllWithOwn(@Parameter(hidden = true) @RequestHeader(
      value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<User> users = userManagementService.selectUserAllWithOwn(lUserId, lOrganizationId);

    List<UserInfoDto> userInfoDtos = users.stream().map(u -> {
      return new UserInfoDto(u);
    }).toList();

    return ApiResponseData.createSuccess(userInfoDtos);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/list-included-in-the-organization",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByIncludedOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody UserInfoPageableDto userPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Page<User> users = userManagementService.selectUserByConditions(lUserId, lOrganizationId, true,
        userPageableDto.getOrganizationId(), userPageableDto.toEntity(),
        userPageableDto.toPageable());

    Page<UserInfoDto> usersDtoPage = users.map(u -> {
      return new UserInfoDto(u);
    });

    return ApiResponseData.createSuccess(usersDtoPage);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/list-not-included-in-the-organization",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByNotIncludedOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody UserInfoPageableDto userPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Page<User> users = userManagementService.selectUserByConditions(lUserId, lOrganizationId, false,
        userPageableDto.getOrganizationId(), userPageableDto.toEntity(),
        userPageableDto.toPageable());

    Page<UserInfoDto> usersDtoPage = users.map(u -> {
      return new UserInfoDto(u);
    });

    return ApiResponseData.createSuccess(usersDtoPage);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/check-duplicate-loginid", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> checkDuplicateLoginId(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Boolean duplicate = userManagementService.checkDuplicateLoginId(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(duplicate);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "user/list-page-filter", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByConditionsUsingCriteria(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<User> items = userManagementService.selectUserByConditions(lUserId, lOrganizationId, true,
        lOrganizationId, criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<UserInfoDto> usersDtoPage = items.map(u -> {
      UserInfoDto userInfoDto = new UserInfoDto(u);
      userInfoDto.setIndex(index.incrementAndGet());
      return userInfoDto;
    });
    return ApiResponseData.createSuccess(usersDtoPage);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/list-all-own-filter", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserAllWithOwn(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<User> items = userManagementService.selectUserAllWithOwn(lUserId, lOrganizationId,
        criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<UserInfoDto> usersDtoPage = items.map(u -> {
      UserInfoDto userInfoDto = new UserInfoDto(u);
      userInfoDto.setIndex(index.incrementAndGet());
      return userInfoDto;
    });
    return ApiResponseData.createSuccess(usersDtoPage);

  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/list-included-in-the-organization-filter",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByIncludedOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<User> items = userManagementService.selectUserByConditions(lUserId, lOrganizationId, true,
        lOrganizationId, criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<UserInfoDto> usersDtoPage = items.map(u -> {
      UserInfoDto userInfoDto = new UserInfoDto(u);
      userInfoDto.setIndex(index.incrementAndGet());
      return userInfoDto;
    });
    return ApiResponseData.createSuccess(usersDtoPage);
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/user/list-not-included-in-the-organization-filter",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByNotIncludedOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<User> items = userManagementService.selectUserByConditions(lUserId, lOrganizationId, false,
        lOrganizationId, criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<UserInfoDto> usersDtoPage = items.map(u -> {
      UserInfoDto userInfoDto = new UserInfoDto(u);
      userInfoDto.setIndex(index.incrementAndGet());
      return userInfoDto;
    });
    return ApiResponseData.createSuccess(usersDtoPage);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      description = "parameter = 조직ID")
  @PostMapping(path = "/user/list-by-organization-filter",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserByOrganizationWithOwn(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<User> items = userManagementService.selectUserByOrganizationWithOwn(lUserId,
        lOrganizationId, lOrganizationId, criterias, gridFiltersPageableDto.toPageable());

    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<UserInfoDto> usersDtoPage = items.map(u -> {
      UserInfoDto userInfoDto = new UserInfoDto(u);
      userInfoDto.setIndex(index.incrementAndGet());
      return userInfoDto;
    });

    return ApiResponseData.createSuccess(usersDtoPage);
  }
}
