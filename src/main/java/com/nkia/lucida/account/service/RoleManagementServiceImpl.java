package com.nkia.lucida.account.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.nkia.lucida.account.constants.AccountConstant;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.kafka.AccountKafkaProducer;
import com.nkia.lucida.account.repository.GroupRepository;
import com.nkia.lucida.account.repository.PermissionRepository;
import com.nkia.lucida.account.repository.RoleRepository;
import com.nkia.lucida.account.repository.UserRepository;
import com.nkia.lucida.avro.ActionType;
import com.nkia.lucida.avro.TargetType;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalErrorCode;

@Service
public class RoleManagementServiceImpl implements RoleManagementService {


  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PermissionRepository permissionRepository;
  private final GroupRepository groupRepository;
  private final AccountKafkaProducer accountKafkaProducer;

  public RoleManagementServiceImpl(RoleRepository roleRepository, UserRepository userRepository,
      PermissionRepository permissionRepository, GroupRepository groupRepository,
      AccountKafkaProducer accountKafkaProducer) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.permissionRepository = permissionRepository;
    this.groupRepository = groupRepository;
    this.accountKafkaProducer = accountKafkaProducer;
  }



  @Override
  public Role insertRole(String lUserId, String lOrganizationId, Role role, List<String> userIds,
      List<String> permissionIds, List<String> groupIds) {

    Assert.notNull(role, "Role must not be null.");

    if (checkDuplicateName(lUserId, lOrganizationId, role.getName())) {
      throw new AuthException("Duplicate name. (" + role.getName() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, role.getName());
    }

    long time = Instant.now().getEpochSecond();
    role.setCtime(time);
    role.setMtime(time);

    role.excludeUserAll();
    if (userIds != null && !userIds.isEmpty()) {
      role.addUser(userRepository
          .findUserByLoginIdAndDtimeIsNull(AccountConstant.SYSTEM_ADMIN_USER_LOGINID));
      role.addUser(
          userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId));
    }

    role.excludePermissionAll();
    if (permissionIds != null && !permissionIds.isEmpty()) {
      role.addPermission(permissionRepository.findByIdIn(permissionIds));
    }

    role.excludeGroupAll();
    if (groupIds != null && !groupIds.isEmpty()) {
      role.addGroup(groupRepository.findByIdInAndDtimeIsNull(groupIds));
    }

    roleRepository.save(role);

    // 역할 생성 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ROLE, ActionType.INSERT,
        role.getId());

    return role;
  }


  @Transactional
  @Override
  public Role updateRole(String lUserId, String lOrganizationId, Role role, List<String> userIds,
      List<String> permissionIds, List<String> groupIds) {

    Assert.notNull(role, "Role must not be null.");
    Assert.notNull(role.getId(), "Role id must not be null.");

    Role target = roleRepository.findRoleById(role.getId());
    if (!target.getName().equals(role.getName())
        && checkDuplicateName(lUserId, lOrganizationId, role.getName())) {
      throw new AuthException("Duplicate name. (" + role.getName() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, role.getName());
    }

    BeanUtils.copyProperties(role, target, "ctime", "users", "permissions", "groups");

    target.excludeUserAll();
    if (userIds != null && !userIds.isEmpty()) {
      target.addUser(
          userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId));
    }

    target.excludePermissionAll();
    if (permissionIds != null && !permissionIds.isEmpty()) {
      target.addPermission(permissionRepository.findByIdIn(permissionIds));
    }

    target.excludeGroupAll();
    if (groupIds != null && !groupIds.isEmpty()) {
      target.addGroup(groupRepository.findByIdInAndDtimeIsNull(groupIds));
    }

    roleRepository.save(target);

    // 역할 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ROLE, ActionType.UPDATE,
        target.getId());
    return target;
  }



  @Override
  public void deleteRole(String lUserId, String lOrganizationId, boolean ignoreUser,
      List<String> roleIds) {

    Assert.notNull(roleIds, "Role must not be null.");
    Assert.notEmpty(roleIds, "Role must not be empty.");

    long time = Instant.now().getEpochSecond();
    List<Role> roles = roleRepository.findByIdInAndDtimeIsNull(roleIds);
    for (Role role : roles) {
      if (!ignoreUser && role.getUsers() != null && role.getUsers().size() > 0) {
        throw new AuthException("Could not delete role. Remove the user.",
            GlobalErrorCode.CAN_NOT_BE_DELETED, role.getId());
      }
      role.setDtime(time);
    }

    roleRepository.saveAll(roles);

    // 역할 삭제 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.ROLE, ActionType.DELETE,
        roleIds);
  }


  @Override
  public Role selectRoleById(String lUserId, String lOrganizationId, String id) {
    return roleRepository.findRoleById(id);
  }


  @Override
  public List<Role> selectRoleByConditions(String lUserId, String lOrganizationId, Role role) {
    return roleRepository.findByDtimeIsNullOrderByNameAsc();
  }


  @Override
  public List<Role> selectRoleByUser(String lUserId, String lOrganizationId, User user) {
    return roleRepository.findByUsers_idAndDtimeIsNull(user.getId());
  }



  @Override
  public List<Role> selectRoleByUserWithOwn(String lUserId, String lOrganizationId, User user) {

    List<Role> own = roleRepository.findByUsers_idAndDtimeIsNull(user.getId());
    List<Role> notOwn = roleRepository.findByUsers_idNotAndDtimeIsNull(user.getId());
    List<Role> roles = new ArrayList<>();

    own = own.stream().map(r -> {
      r.setOwn(true);
      return r;
    }).toList();
    roles.addAll(own);

    notOwn.stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
    roles.addAll(notOwn);
    return roles;
  }


  @Override
  public List<Role> selectRoleByGroupWithOwn(String lUserId, String lOrganizationId, Group group) {

    List<Role> own = roleRepository.findByGroups_idAndDtimeIsNull(group.getId());
    List<Role> notOwn = roleRepository.findByGroups_idNotAndDtimeIsNull(group.getId());
    List<Role> roles = new ArrayList<>();

    own = own.stream().map(r -> {
      r.setOwn(true);
      return r;
    }).toList();
    roles.addAll(own);

    notOwn.stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
    roles.addAll(notOwn);
    return roles;
  }


  @Override
  public Boolean checkDuplicateName(String lUserId, String lOrganizationId, String name) {
    Role role = roleRepository.findFirstByNameAndDtimeIsNull(name);
    return (role != null);
  }



  @Override
  public List<String> selectRoleByUserIncludeIdFileds(String userId) {
    return roleRepository.selectByUsers_idIncludeIdFields(userId).stream().map(i -> {
      return i.getId();
    }).toList();

  }


  @Override
  public Page<Role> selectRoleByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {
    return roleRepository.selectByConditions(lUserId, lOrganizationId, criterias, pageable);
  }
}
