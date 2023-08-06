package com.nkia.lucida.account.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nkia.lucida.account.constants.AccountConstant;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.repository.GroupRepository;
import com.nkia.lucida.account.repository.OrganizationRepository;
import com.nkia.lucida.account.repository.PermissionRepository;
import com.nkia.lucida.account.repository.RoleRepository;
import com.nkia.lucida.account.repository.UserRepository;
import com.nkia.lucida.common.auth.SecurityContext;
import com.nkia.lucida.common.mongodb.TenantContextHolder;
import com.nkia.lucida.common.mongodb.TenantMongoDBConstants;
import com.nkia.lucida.common.mongodb.TenantMongoDBUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InitializeServiceImpl implements InitializeService {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final GroupRepository groupRepository;
  private final ApplicationContext applicationContext;

  public InitializeServiceImpl(
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_ISOLATION) MongoTemplate mongoTemplateIsolation,
      UserRepository userRepository, OrganizationRepository organizationRepository,
      RoleRepository roleRepository, PermissionRepository permissionRepository,
      GroupRepository groupRepository, ApplicationContext applicationContext) {

    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.groupRepository = groupRepository;
    this.applicationContext = applicationContext;
  }


  @EventListener(ApplicationReadyEvent.class)
  private void applicationReadyEventListener(ApplicationReadyEvent event) {

    log.info("Shared database is {}.", AccountConstant.DATABASE_SHARED);

    initializeAccountData();



  }


  private void refreshPermissions(Set<Permission> cPermissions, Set<Permission> nPermissions) {

    // cPermissions 현재 권한 목록
    // nPermissions 적용될 권한 목록

    // 삭제 대상이 담길 목록
    Set<Permission> dPermissions = new HashSet<>(cPermissions);

    // 추가 대상이 담길 목록
    Set<Permission> aPermissions = new HashSet<>(nPermissions);

    // 현재 권한 목록에서 사용되지 않을 권한을 추출하여 삭제 (Asymmetric difference)
    // dPermissions = {A,B,C} removeAll nPermissions = {B,C,D} >>> dPermissions = {A}
    dPermissions.removeAll(nPermissions);

    // 추가될 권한 목록에서 현재 사용되는 것을 제외(Asymmetric difference)
    // aPermissions = {A,B,C} removeAll cPermissions = {B,C,D} >>> aPermissions = {A}
    aPermissions.removeAll(cPermissions);


    // 모든 조직의 역할에서 사요하지 않는 권한을 제거
    if (!dPermissions.isEmpty()) {
      List<Organization> organizations =
          organizationRepository.selectByDtimeIsNullIncludeIdFields();
      for (Organization organization : organizations) {
        TenantContextHolder.INSTANCE.setTenantId(organization.getId());
        List<Role> roles =
            roleRepository.findByPermissionsInAndDtimeIsNull(new ArrayList<>(dPermissions));
        for (Role role : roles) {
          role.excludePermission(dPermissions);
        }
        roleRepository.saveAll(roles);
        TenantContextHolder.INSTANCE.clear();
      }
    }

    if (!dPermissions.isEmpty() || !aPermissions.isEmpty()) {
      permissionRepository.deleteAll();
      permissionRepository.saveAll(nPermissions);
    }
  }



  private Set<Permission> getAllermission() {
    return new HashSet<>(permissionRepository.findAll());
  }



  private Set<Permission> getPermissionsFromFile(String jsonFileName, ObjectMapper objectMapper) {

    String fileName = jsonFileName == null ? "permission.json" : jsonFileName;
    Reader reader = null;
    Set<Permission> permissions = null;
    try {
      Resource resource = new FileSystemResource(fileName);
      if (!resource.exists()) {
        resource = new ClassPathResource(fileName);
      }

      if (!resource.exists()) {
        return new HashSet<>();
      }

      reader = new InputStreamReader(resource.getInputStream(),
          Charset.forName(StandardCharsets.UTF_8.name()));

      permissions =
          new HashSet<>(Arrays.asList(objectMapper.readValue(reader, Permission[].class)));

    } catch (IOException e) {
      log.error("", e);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException ignore) {
      }
    }
    return permissions == null ? new HashSet<>() : permissions;
  }



  @Override
  public void initializeAccountData() {


    Set<Permission> cPermissions = getAllermission();

    Set<Permission> nPermissions =
        getPermissionsFromFile("permission.json", applicationContext.getBean(ObjectMapper.class));

    refreshPermissions(cPermissions, nPermissions);

    User systemAdminUser = createUser(AccountConstant.SYSTEM_ADMIN_USER_LOGINID,
        AccountConstant.SYSTEM_ADMIN_USER_NAME);

    User organizationAdminUser = createUser(AccountConstant.DEFAULT_ORGANIZATION_ADMIN_USER_LOGINID,
        AccountConstant.DEFAULT_ORGANIZATION_ADMIN_USER_NAME);

    Organization organization = createOrganization(AccountConstant.DEFAULT_ORGANIZATION_NAME);

    setupOrganizationAndUser(organization, systemAdminUser);
    setupOrganizationAndUser(organization, organizationAdminUser);

    Role administratorsRole = createRole(organization, AccountConstant.ROLE_ADMINISTRATORS);

    setupRoleAndUser(organization, administratorsRole, systemAdminUser);
    setupRoleAndUser(organization, administratorsRole, organizationAdminUser);
    setupRoleAndPermission(organization, administratorsRole, nPermissions);


    Group everyOneGroup = createGroup(organization, AccountConstant.GROUP_EVERYONE);

    setupGroupAndUser(organization, everyOneGroup, systemAdminUser);
    setupGroupAndUser(organization, everyOneGroup, organizationAdminUser);

    addMissedSystemAdmin(organization, systemAdminUser);
  }



  private User createUser(String loginId, String userName) {

    TenantContextHolder.INSTANCE.setTenantId(AccountConstant.DATABASE_SHARED);
    long currentUnixTime = Instant.now().getEpochSecond();
    User user = userRepository.findUserByLoginIdAndDtimeIsNull(loginId);

    if (user != null) {
      log.info("Exists user. [loginId:{}, name:{}]", user.getLoginId(), user.getName());
      TenantContextHolder.INSTANCE.clear();
      return user;
    }

    String salt = SecurityContext.INSTANCE.generateSalt();
    String password =
        SecurityContext.INSTANCE.hash(SecurityContext.INSTANCE.hashFromUI(loginId), salt);

    user = User.builder().build();
    user.setLoginId(loginId);
    user.setName(userName);
    user.setDescription("My name is " + userName + ".");
    user.setLocked(false);
    user.setNeedChangePassword(false);
    user.setCtime(currentUnixTime);
    user.setMtime(currentUnixTime);
    user.setPassword(password);
    user.setSalt(salt);
    userRepository.save(user);

    TenantContextHolder.INSTANCE.clear();
    log.info("Create user. [loginId:{}, name:{}]", user.getLoginId(), user.getName());
    return user;
  }


  private Organization createOrganization(String organizationName) {

    TenantContextHolder.INSTANCE.setTenantId(AccountConstant.DATABASE_SHARED);
    long currentUnixTime = Instant.now().getEpochSecond();
    Organization organization =
        organizationRepository.findFirstByNameAndDtimeIsNull(organizationName);

    if (organization != null) {
      log.info("Exists organization. [id:{}, name:{}]", organization.getId(),
          organization.getName());
      TenantContextHolder.INSTANCE.clear();
      return organization;
    }

    organization = Organization.builder().build();
    organization.setName(organizationName);
    organization.setDescription("This is an organization created by default.");
    organization.setCtime(currentUnixTime);
    organization.setMtime(currentUnixTime);
    organizationRepository.save(organization);

    TenantMongoDBUtils.INSTANCE.createMongoDBDatabase(applicationContext, organization.getId());

    log.info("Create Organization. [organizationId:{}, organizationName:{}]", organization.getId(),
        organization.getName());
    TenantContextHolder.INSTANCE.clear();
    return organization;

  }


  private Role createRole(Organization organization, String roleName) {

    TenantContextHolder.INSTANCE.setTenantId(organization.getId());
    long currentUnixTime = Instant.now().getEpochSecond();
    Role role = roleRepository.findFirstByNameAndDtimeIsNull(roleName);

    if (role != null) {
      log.info("Exists role. [organizationId:{}, roleId:{}, roleName:{}]", organization.getId(),
          role.getId(), role.getName());
      TenantContextHolder.INSTANCE.clear();
      return role;
    }

    role = Role.builder().build();
    role.setName(roleName);
    role.setDescription("");
    role.setCtime(currentUnixTime);
    role.setMtime(currentUnixTime);
    roleRepository.save(role);
    TenantContextHolder.INSTANCE.clear();

    log.info("Create role. [organizationId:{}, roleId:{}, roleName:{}]", organization.getId(),
        role.getId(), role.getName());

    return role;
  }



  private Group createGroup(Organization organization, String groupName) {
    TenantContextHolder.INSTANCE.setTenantId(organization.getId());
    Group group = groupRepository.findFirstByNameAndDtimeIsNull(groupName);
    if (group != null) {
      log.info("Exists group. [organizationId:{}, groupId:{}, groupName:{}]", organization.getId(),
          group.getId(), group.getName());
      TenantContextHolder.INSTANCE.clear();
      return group;
    }


    group = Group.builder().build();
    group.setName(groupName);
    group.setDescription("");

    long time = Instant.now().getEpochSecond();
    group.setCtime(time);
    group.setMtime(time);
    groupRepository.save(group);
    TenantContextHolder.INSTANCE.clear();

    return group;
  }



  private void setupOrganizationAndUser(Organization organization, User user) {

    if (organization == null || user == null) {
      log.info("Data is null. [organization:{}, user:{}]", organization, user);
      return;
    }

    Assert.notNull(organization.getId(), "Organization id must not be null.");
    Assert.notNull(user.getId(), "User id must not be null.");

    if (!organization.hasUser(user.getId())) {
      organization.addUser(user);
      organizationRepository.save(organization);
    }

    if (!user.hasOrganization(organization.getId())) {
      user.addOrganization(organization);
      userRepository.save(user);
    }
  }


  private void setupRoleAndUser(Organization organization, Role role, User user) {

    if (organization == null || role == null || user == null) {
      log.info("Data is null. [organization:{}, role:{}, user:{}]", organization, role, user);
      return;
    }

    Assert.notNull(organization.getId(), "Organization id must not be null.");
    Assert.notNull(role.getId(), "Role id must not be null.");
    Assert.notNull(user.getId(), "User id must not be null.");

    TenantContextHolder.INSTANCE.setTenantId(organization.getId());

    if (!role.hasUser(user.getId())) {
      role.addUser(user);
      roleRepository.save(role);
    }

    TenantContextHolder.INSTANCE.clear();
  }


  private void setupRoleAndPermission(Organization organization, Role role,
      Collection<Permission> permissions) {

    if (organization == null || role == null || permissions == null) {
      log.info("Data is null. [organization:{}, role:{}, permissions:{}]", organization, role,
          permissions);
      return;
    }

    Assert.notNull(organization.getId(), "Organization id must not be null.");
    Assert.notNull(role.getId(), "Role id must not be null.");

    TenantContextHolder.INSTANCE.setTenantId(organization.getId());

    boolean check = false;
    for (Permission permission : permissions) {
      if (!role.hasPermission(permission.getId())) {
        check = true;
        break;
      }
    }

    if (check) {
      role.addPermission(permissions);
      roleRepository.save(role);
    }

    TenantContextHolder.INSTANCE.clear();
  }



  private void setupGroupAndUser(Organization organization, Group group, User user) {

    if (organization == null || group == null || user == null) {
      log.info("Data is null. [organization:{}, group:{}, user:{}]", organization, group, user);
      return;
    }

    Assert.notNull(organization.getId(), "Organization id must not be null.");
    Assert.notNull(group.getId(), "Group id must not be null.");
    Assert.notNull(user.getId(), "User id must not be null.");

    TenantContextHolder.INSTANCE.setTenantId(organization.getId());

    if (!group.hasUser(user.getId())) {
      group.addUser(user);
      groupRepository.save(group);
    }

    TenantContextHolder.INSTANCE.clear();
  }



  private void addMissedSystemAdmin(Organization organization, User systemAdministratorUser) {
    // 전체 조직을 탐색하여 시스템 관리자의 누락 여부를 확인하여 추가
    long currentUnixTime = Instant.now().getEpochSecond();
    List<Organization> organizations = organizationRepository.findByDtimeIsNullOrderByNameAsc();
    for (Organization o : organizations) {
      if (!o.hasUser(systemAdministratorUser.getId())) {
        o.addUser(systemAdministratorUser);
        o.setMtime(currentUnixTime);
        organizationRepository.save(o);

        systemAdministratorUser.addOrganization(o);
        systemAdministratorUser.setMtime(currentUnixTime);
        userRepository.save(systemAdministratorUser);

        log.info("Add systemAdministrator. [organization:{}, user:{}]", organization,
            systemAdministratorUser);
      }
    }
  }



}
