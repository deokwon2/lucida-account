package com.nkia.lucida.account.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nkia.lucida.account.constants.AccountConstant;
import com.nkia.lucida.account.dto.RoleInfoDto;
import com.nkia.lucida.account.dto.UserInfoDto;
import com.nkia.lucida.account.dto.UserSaveDto;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.repository.OrganizationRepository;
import com.nkia.lucida.account.service.InitializeService;
import com.nkia.lucida.account.service.OrganizationManagementService;
import com.nkia.lucida.account.service.RoleManagementService;
import com.nkia.lucida.account.service.UserManagementService;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.auth.SecurityContext;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.mongodb.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/account")
@Tag(name = "99. Development", description = "")
public class DevelopmentController {

  private final RoleManagementService roleManagementService;
  private final UserManagementService userManagementService;
  private final ApplicationContext applicationContext;

  public DevelopmentController(RoleManagementService roleManagementService,
      UserManagementService userManagementService, JwtTokenService jwtTokenService,
      ApplicationContext applicationContext) {
    this.roleManagementService = roleManagementService;
    this.userManagementService = userManagementService;
    this.applicationContext = applicationContext;
  }

  @GetMapping(path = "/test/1/password/to-base64")
  public ApiResponseData<String> tooBase64(@RequestParam String pwd) {
    return ApiResponseData.createSuccess(SecurityContext.INSTANCE.hashFromUI(pwd));
  }



  @PostMapping(path = "/test/2/init-account-data")
  public ApiResponseData<Object> methodTest() {
    applicationContext.getBean(InitializeService.class).initializeAccountData();
    return ApiResponseData.createSuccess(true);
  }

  @PostMapping(path = "/test/3/organization/insert")
  public ApiResponseData<Object> organizationInsert(
      @RequestParam(required = true) String organizationName,
      @RequestParam(required = false) String addOrganizationAdminLoginId,
      @RequestParam(required = false) String addOrganizationAdminLoginName) {

    TenantContextHolder.INSTANCE.setTenantId(AccountConstant.DATABASE_SHARED);

    applicationContext.getBean(OrganizationManagementService.class).insertOrganization(null, null,
        null, null);

    TenantContextHolder.INSTANCE.clear();
    return ApiResponseData.createSuccess(true);
  }


  @PostMapping(path = "/test/4/organization/list")
  public ApiResponseData<Object> organizationList(
      @RequestParam(required = true) String organizationId,
      @RequestParam(required = false) List<String> roleIds) {

    TenantContextHolder.INSTANCE.setTenantId(organizationId);
    List<Organization> oraOrganizations = applicationContext.getBean(OrganizationRepository.class)
        .selectByDtimeIsNullIncludeIdFields();
    TenantContextHolder.INSTANCE.clear();
    return ApiResponseData.createSuccess(oraOrganizations);
  }



  @PostMapping(path = "/test/5/user/insert-bulk")
  public ApiResponseData<Object> insertSampeUsers(
      @RequestParam(required = true) String lOrganizationId,
      @RequestParam(required = true) String loginIdPrefix,
      @RequestParam(required = true) String userNamePrefix,
      @RequestParam(required = true) int createCount,
      @RequestParam(required = true) List<String> roleIds) {

    TenantContextHolder.INSTANCE.setTenantId(lOrganizationId);

    List<User> users = new ArrayList<>();
    for (int i = 0; i < createCount; i++) {
      String stringNumber = String.format("%04d", i);
      UserSaveDto user = new UserSaveDto();
      user.setLoginId(loginIdPrefix + "-" + stringNumber);
      user.setName(userNamePrefix + "-" + stringNumber);
      user.setDescription("My name is " + user.getName() + "(" + user.getLoginId() + ")");
      user.setEmail(user.getLoginId() + "@email.co.kr");
      user.setPhone("010-" + stringNumber + "-" + stringNumber);
      user.setPassword(SecurityContext.INSTANCE.hashFromUI(user.getLoginId()));
      user.setLocked(false);
      user.setNeedChangePassword(false);
      users.add(user.toEntity());
    }

    userManagementService.insertUser(null, lOrganizationId, users, roleIds);
    TenantContextHolder.INSTANCE.clear();

    return ApiResponseData.createSuccess(null);
  }


  @PostMapping(path = "/test/6/tenant-check/role")
  public ApiResponseData<Object> tenantCheckRole(@RequestParam(required = true) boolean useTenantId,
      @RequestParam(required = true) String lOrganizationId) {
    if (useTenantId) {
      TenantContextHolder.INSTANCE.setTenantId(lOrganizationId);
    }

    List<Role> items =
        roleManagementService.selectRoleByConditions(lOrganizationId, lOrganizationId, null);

    List<RoleInfoDto> itemDtos = items.stream().map(o -> {
      return new RoleInfoDto(o);
    }).toList();

    if (useTenantId) {
      TenantContextHolder.INSTANCE.clear();
    }
    return ApiResponseData.createSuccess(itemDtos);
  }



  @PostMapping(path = "/test/7/tenant-check/user")
  public ApiResponseData<Object> tenantCheckUser(@RequestParam(required = true) boolean useTenantId,
      @RequestParam(required = true) String lOrganizationId) {
    if (useTenantId) {
      TenantContextHolder.INSTANCE.setTenantId(lOrganizationId);
    }

    List<User> users = userManagementService.selectUserAll("system", lOrganizationId);
    List<UserInfoDto> userInfoDtos = users.stream().map(u -> {
      return new UserInfoDto(u);
    }).toList();

    if (useTenantId) {
      TenantContextHolder.INSTANCE.clear();
    }
    return ApiResponseData.createSuccess(userInfoDtos);
  }
}
