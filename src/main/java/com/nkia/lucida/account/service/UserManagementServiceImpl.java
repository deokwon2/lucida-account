package com.nkia.lucida.account.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.kafka.AccountKafkaProducer;
import com.nkia.lucida.account.repository.GroupRepository;
import com.nkia.lucida.account.repository.OrganizationRepository;
import com.nkia.lucida.account.repository.RoleRepository;
import com.nkia.lucida.account.repository.UserRepository;
import com.nkia.lucida.avro.ActionType;
import com.nkia.lucida.avro.TargetType;
import com.nkia.lucida.common.auth.SecurityContext;
import com.nkia.lucida.common.exception.AuthErrorCode;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalErrorCode;

@Service
public class UserManagementServiceImpl implements UserManagementService {


  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final OrganizationRepository organizationRepository;
  private final GroupRepository groupRepository;
  private final AccountKafkaProducer accountKafkaProducer;

  public UserManagementServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
      OrganizationRepository organizationRepository, GroupRepository groupRepository,
      AccountKafkaProducer accountKafkaProducer) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.organizationRepository = organizationRepository;
    this.groupRepository = groupRepository;
    this.accountKafkaProducer = accountKafkaProducer;
  }


  @Transactional
  @Override
  public User insertUser(String lUserId, String lOrganizationId, User user, List<String> roleIds,
      List<String> groupIds) {

    Assert.notNull(user, "User must not be null.");
    Assert.notNull(roleIds, "Role id must not be null.");
    Assert.notEmpty(roleIds, "Role id must not be empty.");

    Organization organization = organizationRepository.findById(lOrganizationId).orElse(null);
    Assert.notNull(organization, "Organization (" + lOrganizationId + ") must not be null");

    if (checkDuplicateLoginId(lUserId, lOrganizationId, user.getLoginId())) {
      throw new AuthException("Duplicate loginId. (" + user.getLoginId() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, user.getLoginId());
    }

    long time = Instant.now().getEpochSecond();
    user.setCtime(time);
    user.setMtime(time);
    user.addOrganization(organization);

    String salt = SecurityContext.INSTANCE.generateSalt();
    String password = SecurityContext.INSTANCE.hash(user.getPassword(), salt);
    user.setPassword(password);
    user.setSalt(salt);
    userRepository.save(user);

    List<Role> roles = roleRepository.findByIdInAndDtimeIsNull(roleIds);
    for (Role role : roles) {
      role.addUser(user);
    }
    roleRepository.saveAll(roles);

    List<Group> groups = groupRepository.findByIdInAndDtimeIsNull(groupIds);
    for (Group group : groups) {
      group.addUser(user);
    }
    groupRepository.saveAll(groups);

    organization.addUser(user);
    organizationRepository.save(organization);



    // 사용자 생성 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.INSERT,
        user.getId());
    return user;
  }



  @Transactional
  @Override
  public void insertUser(String lUserId, String lOrganizationId, List<User> users,
      List<String> roleIds) {

    Assert.notNull(users, "User must not be null.");
    Assert.notNull(roleIds, "Role id must not be null.");
    Assert.notEmpty(roleIds, "Role id must not be empty.");

    Organization organization = organizationRepository.findById(lOrganizationId).orElse(null);
    Assert.notNull(organization, "Organization (" + lOrganizationId + ") must not be null");

    long time = Instant.now().getEpochSecond();
    for (User user : users) {
      if (checkDuplicateLoginId(lUserId, lOrganizationId, user.getLoginId())) {
        throw new AuthException("Duplicate loginId. (" + user.getLoginId() + ")",
            GlobalErrorCode.CAN_NOT_BE_SAVED, user.getLoginId());
      }
      user.setCtime(time);
      user.setMtime(time);
      user.addOrganization(organization);

      String salt = SecurityContext.INSTANCE.generateSalt();
      String password = SecurityContext.INSTANCE.hash(user.getPassword(), salt);
      user.setPassword(password);
      user.setSalt(salt);
    }
    userRepository.saveAll(users);

    List<Role> roles = roleRepository.findByIdInAndDtimeIsNull(roleIds);
    for (Role role : roles) {
      role.addUser(users);
    }
    roleRepository.saveAll(roles);

    organization.addUser(users);
    organizationRepository.save(organization);

    // 사용자 생성 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.INSERT,
        users.stream().map(u -> u.getId()).toList());
  }


  @Transactional
  @Override
  public User updateUser(String lUserId, String lOrganizationId, User user, List<String> roleIds,
      List<String> groupIds) {

    Assert.notNull(user, "User must not be null.");
    Assert.notNull(user.getId(), "User id must not be null.");

    Organization organization = organizationRepository.findById(lOrganizationId).orElse(null);
    Assert.notNull(organization, "Organization (" + lOrganizationId + ") must not be null.");

    User target = userRepository.findUserById(user.getId());
    if (!target.getLoginId().equals(user.getLoginId())
        && checkDuplicateLoginId(lUserId, lOrganizationId, user.getLoginId())) {
      throw new AuthException("Duplicate loginId. (" + user.getLoginId() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, user.getLoginId());
    }

    BeanUtils.copyProperties(user, target, "lastLoginTime", "ctime", "password", "salt",
        "organizations");

    // 사용자 정보 저장
    target.setMtime(Instant.now().getEpochSecond());
    userRepository.save(target);

    // 모든 Role에서 변경하려는 사용자정보 제거
    List<Role> excludeRoles = roleRepository.findByUsers_idAndDtimeIsNull(target.getId());
    for (Role role : excludeRoles) {
      role.excludeUser(target);
    }
    roleRepository.saveAll(excludeRoles);
    excludeRoles.clear();

    // 새롭게 선택된 Role에 변경하려는 사용자 추가
    if (roleIds != null && !roleIds.isEmpty()) {
      List<Role> addRoles = roleRepository.findByIdInAndDtimeIsNull(roleIds);
      for (Role role : addRoles) {
        role.addUser(target);
      }
      roleRepository.saveAll(addRoles);
      addRoles.clear();
    }


    // 모든 그룹에서 변경하려는 사용자정보 제거
    List<Group> excludeGroups = groupRepository.findByUsers_idAndDtimeIsNull(target.getId());
    for (Group group : excludeGroups) {
      group.excludeUser(target);
    }
    groupRepository.saveAll(excludeGroups);
    excludeGroups.clear();



    // 새롭게 선택된 Group에 변경하려는 사용자 추가
    if (groupIds != null && !groupIds.isEmpty()) {
      List<Group> addGroups = groupRepository.findByIdInAndDtimeIsNull(groupIds);
      for (Group group : addGroups) {
        group.addUser(target);
      }
      groupRepository.saveAll(addGroups);
      addGroups.clear();
    }

    // 조직의 사용자 정보 갱신
    organization.excludeUser(target);
    organization.addUser(target);
    organizationRepository.save(organization);


    // 사용자 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.UPDATE,
        target.getId());
    return target;
  }



  @Transactional
  @Override
  public User updateUserPassword(String lUserId, String lOrganizationId, String userId,
      String currentPassword, String newPassword) {

    User user = userRepository.findUserById(userId);
    if (user == null) {
      throw new AuthException("User Not Found.", GlobalErrorCode.NO_DATA_FOUND, userId);
    }

    String cPassword = SecurityContext.INSTANCE.hash(currentPassword, user.getSalt());
    String nPassword = SecurityContext.INSTANCE.hash(newPassword, user.getSalt());

    if (!cPassword.equals(user.getPassword())) {
      throw new AuthException("Password Not Match", AuthErrorCode.PASSWORD_NOT_MATCH, userId);
    }

    if (cPassword.equals(nPassword)) {
      throw new AuthException("Same Password.", AuthErrorCode.SAME_PASSWORD, userId);
    }

    String salt = SecurityContext.INSTANCE.generateSalt();
    String password = SecurityContext.INSTANCE.hash(newPassword, salt);
    user.setPassword(password);
    user.setSalt(salt);
    user.setNeedChangePassword(false);
    userRepository.save(user);

    // 사용자 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.UPDATE,
        user.getId());
    return user;
  }



  @Transactional
  @Override
  public User updateUserPasswordByAdmin(String lUserId, String lOrganizationId, String userId,
      String newPassword) {

    User user = userRepository.findUserByLoginIdAndDtimeIsNull(userId);
    if (user == null) {
      throw new AuthException("User Not Found.", GlobalErrorCode.NO_DATA_FOUND, userId);
    }

    String salt = SecurityContext.INSTANCE.generateSalt();
    String password = SecurityContext.INSTANCE.hash(newPassword, salt);
    user.setPassword(password);
    user.setSalt(salt);
    userRepository.save(user);

    // 사용자 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.UPDATE,
        user.getId());
    return user;
  }


  @Transactional
  @Override
  public void deleteUser(String lUserId, String lOrganizationId, boolean ignoreRole,
      List<String> userIds) {

    Assert.notNull(userIds, "User id must not be null.");
    Assert.notEmpty(userIds, "User id must not be empty.");

    Organization organization = organizationRepository.findById(lOrganizationId).orElse(null);
    Assert.notNull(organization, "Organization (" + lOrganizationId + ") must not be null");

    long time = Instant.now().getEpochSecond();
    List<User> users =
        userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId);
    List<Role> roles = new ArrayList<>();
    List<Group> groups = new ArrayList<>();

    for (User user : users) {
      List<Role> findRoles = roleRepository.findByUsers_idAndDtimeIsNull(user.getId());
      List<Group> findGroups = groupRepository.findByUsers_idAndDtimeIsNull(user.getId());

      if (!ignoreRole && (!findRoles.isEmpty() || !findGroups.isEmpty())) {
        throw new AuthException("Could not delete user. Remove the role or group.",
            GlobalErrorCode.CAN_NOT_BE_DELETED, user.getId());
      } else {
        for (Role role : findRoles) {
          role.excludeUser(user);
          roles.add(role);
        }

        for (Group group : findGroups) {
          group.excludeUser(users);
          groups.add(group);
        }
      }

      organization.excludeUser(user);
      user.excludeOrganization(organization);
      user.setDtime(time);
    }
    organizationRepository.save(organization);
    groupRepository.saveAll(groups);
    roleRepository.saveAll(roles);
    userRepository.saveAll(users);

    // 사용자 삭제 topic 발행
    List<String> userLoginIds = users.stream().map(u -> u.getLoginId()).toList();
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.USER, ActionType.DELETE,
        userLoginIds.toArray(new String[userLoginIds.size()]));
  }



  @Override
  public Page<User> selectUserByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, User user, Pageable pageable) {

    return userRepository.selectByConditions(lUserId, lOrganizationId, includeOrganization,
        organizationId, user.getLoginId(), user.getName(), user.getEmail(), user.getPhone(),
        user.getLocked(), pageable);
  }



  @Override
  public List<User> selectUserByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, User user) {
    return userRepository.selectByConditions(lUserId, lOrganizationId, includeOrganization,
        organizationId, user.getLoginId(), user.getName(), user.getEmail(), user.getPhone(),
        user.getLocked());
  }


  @Override
  public User selectUserById(String lUserId, String lOrganizationId, String id) {
    return userRepository.findUserById(id);
  }


  @Override
  public User selectUserByLoginId(String lUserId, String lOrganizationId, String loginId) {
    return userRepository.findUserByLoginIdAndDtimeIsNull(loginId);
  }



  @Override
  public List<User> selectUserByOrganizationWithOwn(String lUserId, String lOrganizationId,
      String organizationId) {

    List<User> own = userRepository.findByOrganizations_idAndDtimeIsNull(organizationId);
    List<User> notOwn = userRepository.findByOrganizations_idNotAndDtimeIsNull(organizationId);
    List<User> users = new ArrayList<>();

    own = own.stream().map(r -> {
      r.setOwn(true);
      return r;
    }).toList();
    users.addAll(own);

    notOwn.stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
    users.addAll(notOwn);
    return users;

  }


  @Override
  public Boolean checkDuplicateLoginId(String lUserId, String lOrganizationId, String loginId) {
    User user = userRepository.findUserByLoginIdAndDtimeIsNull(loginId);
    return (user != null);
  }


  @Override
  public List<User> selectUserByRoleWithOwn(String lUserId, String lOrganizationId, String roleId) {

    List<User> users = new ArrayList<>();
    Role role = roleRepository.findRoleById(roleId);
    if (role.getUsers() != null && !role.getUsers().isEmpty()) {
      List<String> userIds =
          role.getUsers().stream().map(u -> u.getId()).collect(Collectors.toList());

      List<User> own =
          userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId);
      List<User> notOwn =
          userRepository.findByIdNotInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId);
      own = own.stream().map(r -> {
        r.setOwn(true);
        return r;
      }).toList();
      users.addAll(own);

      notOwn.stream().map(r -> {
        r.setOwn(false);
        return r;
      }).toList();
      users.addAll(notOwn);

      return users;
    } else {
      users =
          userRepository.findByOrganizations_idAndDtimeIsNull(lOrganizationId).stream().map(r -> {
            r.setOwn(false);
            return r;
          }).collect(Collectors.toList());
      return users;
    }
  }



  @Override
  public List<User> selectUserByGroupWithOwn(String lUserId, String lOrganizationId,
      String groupId) {

    List<User> users = new ArrayList<>();
    Group group = groupRepository.findGroupById(groupId);
    if (group.getUsers() != null && !group.getUsers().isEmpty()) {
      List<String> userIds =
          group.getUsers().stream().map(u -> u.getId()).collect(Collectors.toList());

      List<User> own =
          userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId);
      List<User> notOwn =
          userRepository.findByIdNotInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId);

      own = own.stream().map(r -> {
        r.setOwn(true);
        return r;
      }).toList();
      users.addAll(own);

      notOwn.stream().map(r -> {
        r.setOwn(false);
        return r;
      }).toList();
      users.addAll(notOwn);
      return users;
    } else {
      users =
          userRepository.findByOrganizations_idAndDtimeIsNull(lOrganizationId).stream().map(r -> {
            r.setOwn(false);
            return r;
          }).collect(Collectors.toList());
      return users;
    }
  }


  @Override
  public List<User> selectUserByGroup(String lUserId, String lOrganizationId, String groupId) {

    Group group = groupRepository.findGroupById(groupId);
    if (group.getUsers() != null && !group.getUsers().isEmpty()) {
      List<String> userIds =
          group.getUsers().stream().map(u -> u.getId()).collect(Collectors.toList());
      return userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId);
    } else {
      return new ArrayList<>();
    }
  }



  @Override
  public List<User> selectUserAllWithOwn(String lUserId, String lOrganizationId) {
    return userRepository.findByOrganizations_idAndDtimeIsNull(lOrganizationId).stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
  }


  @Override
  public Page<User> selectUserByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, List<Criteria> criterias,
      Pageable pageable) {

    return userRepository.selectByConditions(lUserId, lOrganizationId, includeOrganization,
        organizationId, criterias, pageable);
  }



  @Override
  public Page<User> selectUserAllWithOwn(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    return this.selectUserByConditions(lUserId, lOrganizationId, true, lOrganizationId, criterias,
        pageable).map(u -> {
          u.setOwn(false);
          return u;
        });
  }


  @Override
  public Page<User> selectUserByOrganizationWithOwn(String lUserId, String lOrganizationId,
      String organizationId, List<Criteria> criterias, Pageable pageable) {

    return userRepository.selectUserByOrganizationWithOwn(lUserId, lOrganizationId, organizationId,
        criterias, pageable);

  }



  @Override
  public List<User> selectUserAll(String lUserId, String lOrganizationId) {
    return userRepository.findAll();
  }
}
