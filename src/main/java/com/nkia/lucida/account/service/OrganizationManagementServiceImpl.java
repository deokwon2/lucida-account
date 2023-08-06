package com.nkia.lucida.account.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.nkia.lucida.account.constants.AccountConstant;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.kafka.AccountKafkaProducer;
import com.nkia.lucida.account.repository.OrganizationRepository;
import com.nkia.lucida.account.repository.PermissionRepository;
import com.nkia.lucida.account.repository.RoleRepository;
import com.nkia.lucida.account.repository.UserRepository;
import com.nkia.lucida.avro.ActionType;
import com.nkia.lucida.avro.TargetType;
import com.nkia.lucida.common.auth.SecurityContext;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalErrorCode;
import com.nkia.lucida.common.mongodb.TenantContextHolder;
import com.nkia.lucida.common.mongodb.TenantMongoDBUtils;

@Service
public class OrganizationManagementServiceImpl implements OrganizationManagementService {


  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final ApplicationContext applicationContext;
  private final AccountKafkaProducer accountKafkaProducer;

  public OrganizationManagementServiceImpl(OrganizationRepository organizationRepository,
      UserRepository userRepository, RoleRepository roleRepository,
      PermissionRepository permissionRepository, ApplicationContext applicationContext,
      AccountKafkaProducer accountKafkaProducer) {
    this.organizationRepository = organizationRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.applicationContext = applicationContext;
    this.accountKafkaProducer = accountKafkaProducer;
  }



  @Transactional
  @Override
  public Organization insertOrganization(String lUserId, String lOrganizationId,
      Organization organization, List<String> userIds) {
    return insertOrganization(lUserId, lOrganizationId, organization, userIds, null, null, null);
  }



  @Transactional
  @Override
  public Organization insertOrganization(String lUserId, String lOrganizationId,
      Organization organization, List<String> userIds, String addAdminLoginId, String addAdminName,
      String addAdminPassword) {

    Assert.notNull(organization, "Organization must not be null.");

    if (checkDuplicateName(lUserId, lOrganizationId, organization.getName())) {
      throw new AuthException("Duplicate name. (" + organization.getName() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, organization.getName());
    }

    long currentUnixTime = Instant.now().getEpochSecond();
    organization.setId(null);
    organization.setCtime(currentUnixTime);
    organization.setMtime(currentUnixTime);
    organizationRepository.save(organization);

    // 시스템 관리자와 선택된 사용자를 조직에 할당
    User polestar =
        userRepository.findUserByLoginIdAndDtimeIsNull(AccountConstant.SYSTEM_ADMIN_USER_LOGINID);
    List<User> users = userRepository.findByIdInAndDtimeIsNull(userIds);
    if (polestar != null) {
      users.add(polestar);
    }
    for (User user : users) {
      user.addOrganization(organization);
      organization.addUser(user);
    }
    organizationRepository.save(organization);
    userRepository.saveAll(users);

    // 추가 조직 운영 관리자 생성 후 조직할당
    String salt = SecurityContext.INSTANCE.generateSalt();
    String password = SecurityContext.INSTANCE.hash(addAdminPassword, salt);

    User admin = User.builder().build();
    admin.setLoginId(addAdminLoginId);
    admin.setName(addAdminName);
    admin.setPassword(password);
    admin.setSalt(salt);
    admin.setCtime(currentUnixTime);
    admin.setMtime(currentUnixTime);
    admin.setNeedChangePassword(true);
    admin.setLocked(false);
    admin.addOrganization(organization);
    userRepository.save(admin);

    organization.addUser(admin);
    organizationRepository.save(organization);


    // 조직 테넌트 DB 생성
    TenantMongoDBUtils.INSTANCE.createMongoDBDatabase(applicationContext, organization.getId());

    if (polestar != null) {
      // 조직에 속한 관리자 역할 생성
      TenantContextHolder.INSTANCE.setTenantId(organization.getId());
      Role newRole = Role.builder().build();
      newRole.addUser(polestar);
      newRole.addUser(admin);
      newRole.setName(AccountConstant.ROLE_ADMINISTRATORS);
      newRole.setPermissions(createAdminsPermission());
      newRole.setDescription("This is an admin role.");
      roleRepository.save(newRole);
      TenantContextHolder.INSTANCE.clear();
    }

    // 조직 생성 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ORGANIZATION, ActionType.INSERT,
        organization.getId());

    return organization;
  }



  @Transactional
  @Override
  public Organization updateOrganization(String lUserId, String lOrganizationId,
      Organization organization, List<String> userIds) {

    Assert.notNull(organization, "Organization must not be null.");
    Assert.notNull(organization.getId(), "Organization id must not be null.");

    Organization target = organizationRepository.findById(organization.getId()).orElse(null);
    if (!target.getName().equals(organization.getName())
        && checkDuplicateName(lUserId, lOrganizationId, organization.getName())) {
      throw new AuthException("Duplicate name. (" + organization.getName() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, organization.getName());
    }

    BeanUtils.copyProperties(organization, target, "ctime", "users");

    // 기존 사용자에게 있는 조직 정보를 제거
    List<User> excludeUsers = userRepository.findByOrganizations_idAndDtimeIsNull(target.getId());
    for (User user : excludeUsers) {
      user.excludeOrganization(target);
    }
    userRepository.saveAll(excludeUsers);

    // 신규로 선택된 사용자에게 조직 정보 추가
    if (userIds != null && !userIds.isEmpty()) {
      List<User> addUsers = userRepository.findByIdInAndDtimeIsNull(userIds);
      for (User user : addUsers) {
        user.addOrganization(target);
      }
      userRepository.saveAll(addUsers);

      target.excludeUserAll();
      target.addUser(addUsers);
    }

    target.setMtime(Instant.now().getEpochSecond());
    organizationRepository.save(target);

    // 조직 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ORGANIZATION, ActionType.UPDATE,
        target.getId());
    return target;
  }


  @Override
  public void deleteOrganization(String lUserId, String lOrganizationId, boolean ignoreUser,
      List<String> organizationIds) {

    boolean hasId = organizationIds.stream().anyMatch(i -> i.equals(lOrganizationId));
    if (hasId) {
      throw new AuthException("Same OrganizationId.", GlobalErrorCode.CAN_NOT_BE_DELETED,
          lOrganizationId);
    }

    long time = Instant.now().getEpochSecond();
    for (String organizationId : organizationIds) {
      Organization organization = organizationRepository.findOrganizationById(organizationId);
      int count = organization.getUsers().size();
      if (!ignoreUser && count > 0) {
        throw new AuthException("Could not delete organization. Remove the user.",
            GlobalErrorCode.CAN_NOT_BE_DELETED, organizationId);
      } else {

        // 기존 사용자에게 있는 조직 정보를 제거
        List<User> users =
            userRepository.findByOrganizations_idAndDtimeIsNull(organization.getId());
        for (User user : users) {
          user.excludeOrganization(organization);
        }
        userRepository.saveAll(users);


        organization.setDtime(time);
        organizationRepository.save(organization);
      }
    }

    // 조직 삭제 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ORGANIZATION, ActionType.DELETE,
        organizationIds);
  }



  @Override
  public Page<Organization> selectOrganizationByConditions(String lUserId, String lOrganizationId,
      Organization organization, Pageable pageable) {
    // TODO Auto-generated method stub
    return null;
  }



  @Override
  public List<Organization> selectOrganizationByConditions(String lUserId, String lOrganizationId,
      Organization organization) {
    return organizationRepository.findByDtimeIsNullOrderByNameAsc();
  }



  @Override
  public Organization selectOrganizationById(String lUserId, String lOrganizationId,
      String organizationId) {
    return organizationRepository.findOrganizationById(organizationId);
  }


  @Override
  public Boolean checkDuplicateName(String lUserId, String lOrganizationId, String name) {
    Organization organization = organizationRepository.findFirstByNameAndDtimeIsNull(name);
    return (organization != null);
  }



  @Override
  public Organization excludeUserFromOrganization(String lUserId, String lOrganizationId,
      String organizationId, List<String> userIds) {

    Organization organization = organizationRepository.findOrganizationById(organizationId);
    Assert.notNull(organization, "Organization must not be null. (" + organizationId + ")");

    if (userIds != null && !userIds.isEmpty()) {
      List<User> users = userRepository.findByIdInAndDtimeIsNull(userIds);
      for (User user : users) {
        user.excludeOrganization(organization);
      }
      userRepository.saveAll(users);
      organization.excludeUser(users);
    }

    organization.setMtime(Instant.now().getEpochSecond());
    organizationRepository.save(organization);

    // 조직 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ORGANIZATION, ActionType.UPDATE,
        organizationId);

    // 사용자 제거 topic 발행
    List<String> userLoginIds =
        userRepository.findAllById(userIds).stream().map(u -> u.getLoginId()).toList();

    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.DELETE,
        userLoginIds.toArray(new String[userLoginIds.size()]));

    return organization;
  }



  @Override
  public Organization addUserFromOrganization(String lUserId, String lOrganizationId,
      String organizationId, List<String> userIds) {

    Organization organization = organizationRepository.findOrganizationById(organizationId);
    Assert.notNull(organization, "Organization must not be null. (" + organizationId + ")");

    if (userIds != null && !userIds.isEmpty()) {
      List<User> users = userRepository.findByIdInAndDtimeIsNull(userIds);
      for (User user : users) {
        user.addOrganization(organization);
      }
      userRepository.saveAll(users);
      organization.addUser(users);
    }

    organization.setMtime(Instant.now().getEpochSecond());
    organizationRepository.save(organization);

    // 조직 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ORGANIZATION, ActionType.UPDATE,
        organizationId);
    return organization;
  }


  private Set<Permission> createAdminsPermission() {
    return new HashSet<>(permissionRepository.findAll());
  }



  @Override
  public Page<Organization> selectOrganizationByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {
    return organizationRepository.selectByConditions(lUserId, lOrganizationId, criterias, pageable);
  }
}
