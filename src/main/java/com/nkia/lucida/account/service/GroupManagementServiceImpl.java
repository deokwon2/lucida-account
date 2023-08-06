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
import org.springframework.util.Assert;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.kafka.AccountKafkaProducer;
import com.nkia.lucida.account.repository.GroupRepository;
import com.nkia.lucida.account.repository.RoleRepository;
import com.nkia.lucida.account.repository.UserRepository;
import com.nkia.lucida.avro.ActionType;
import com.nkia.lucida.avro.TargetType;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalErrorCode;

@Service
public class GroupManagementServiceImpl implements GroupManagementService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AccountKafkaProducer accountKafkaProducer;

  public GroupManagementServiceImpl(GroupRepository groupRepository, UserRepository userRepository,
      RoleRepository roleRepository, AccountKafkaProducer accountKafkaProducer) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.accountKafkaProducer = accountKafkaProducer;
  }

  @Override
  public Group insertGroup(String lUserId, String lOrganizationId, Group group,
      List<String> userIds, List<String> roleIds) {

    Assert.notNull(group, "Group must not be null.");

    if (checkDuplicateName(lUserId, lOrganizationId, group.getName())) {
      throw new AuthException("Duplicate name. (" + group.getName() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, group.getName());
    }

    group.setId(null);
    group.excludeUserAll();
    if (userIds != null && !userIds.isEmpty()) {
      group.addUser(
          userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId));
    }

    long time = Instant.now().getEpochSecond();
    group.setCtime(time);
    group.setMtime(time);
    groupRepository.save(group);

    List<Role> roles = roleRepository.findByIdInAndDtimeIsNull(roleIds);
    for (Role role : roles) {
      role.addGroup(group);
    }
    roleRepository.saveAll(roles);

    // 그룹 생성 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.GROUP, ActionType.INSERT,
        group.getId());

    return group;
  }



  @Override
  public Group updateGroup(String lUserId, String lOrganizationId, Group group,
      List<String> userIds, List<String> roleIds) {

    Assert.notNull(group, "Group must not be null.");
    Assert.notNull(group.getId(), "Group id must not be null.");

    Group target = groupRepository.findById(group.getId()).orElse(null);
    if (!target.getName().equals(group.getName())
        && checkDuplicateName(lUserId, lOrganizationId, group.getName())) {
      throw new AuthException("Duplicate name. (" + group.getName() + ")",
          GlobalErrorCode.CAN_NOT_BE_SAVED, group.getName());
    }

    BeanUtils.copyProperties(group, target, "ctime", "users");

    target.excludeUserAll();
    if (userIds != null && !userIds.isEmpty()) {
      target.addUser(
          userRepository.findByIdInAndOrganizations_idAndDtimeIsNull(userIds, lOrganizationId));
    }

    target.setMtime(Instant.now().getEpochSecond());
    groupRepository.save(target);

    // 모든 Role에서 변경하려는 그룹정보 제거
    List<Role> excludeRoles = roleRepository.findByGroups_idAndDtimeIsNull(target.getId());
    for (Role role : excludeRoles) {
      role.excludeGroup(target);
    }
    roleRepository.saveAll(excludeRoles);
    excludeRoles.clear();

    // 새롭게 선택된 Role에 변경하려는 그룹추가
    if (roleIds != null && !roleIds.isEmpty()) {
      List<Role> addRoles = roleRepository.findByIdInAndDtimeIsNull(roleIds);
      for (Role role : addRoles) {
        role.addGroup(target);
      }
      roleRepository.saveAll(addRoles);
      addRoles.clear();
    }

    // 그룹 변경 topic 발행
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.GROUP, ActionType.UPDATE,
        group.getId());

    return target;
  }



  @Override
  public void deleteGroup(String lUserId, String lOrganizationId, boolean ignore,
      List<String> groupIds) {

    Assert.notNull(groupIds, "Group id must not be null.");
    Assert.notEmpty(groupIds, "Group id must not be empty.");

    long time = Instant.now().getEpochSecond();
    List<Group> groups = groupRepository.findByIdInAndDtimeIsNull(groupIds);
    List<Role> roles = new ArrayList<>();
    for (Group group : groups) {
      List<Role> findRoles = roleRepository.findByGroups_idAndDtimeIsNull(group.getId());
      if (!ignore && !findRoles.isEmpty()) {
        throw new AuthException("Could not delete group. Remove the role.",
            GlobalErrorCode.CAN_NOT_BE_DELETED, group.getId());
      } else {
        for (Role role : findRoles) {
          role.excludeGroup(group);
          roles.add(role);
        }
      }

      if (!ignore && group.getUsers() != null && !group.getUsers().isEmpty()) {
        throw new AuthException("Could not delete group. Remove the user.",
            GlobalErrorCode.CAN_NOT_BE_DELETED, group.getId());
      } else {
        group.excludeUserAll();
      }
      group.setDtime(time);
    }

    roleRepository.saveAll(roles);
    groupRepository.saveAll(groups);

    // 그룹 삭제 topic 발행 (완전 삭제가 아닌 dtime set)
    accountKafkaProducer.send(lUserId, lOrganizationId, TargetType.GROUP, ActionType.DELETE,
        groupIds);
  }



  @Override
  public Page<Group> selectGroupByConditions(String lUserId, String lOrganizationId, Group group,
      Pageable pageable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Group> selectGroupByConditions(String lUserId, String lOrganizationId, Group group) {
    return groupRepository.findByDtimeIsNullOrderByNameAsc();
  }

  @Override
  public Group selectGroupById(String lUserId, String lOrganizationId, String groupId) {
    return groupRepository.findGroupById(groupId);
  }

  @Override
  public Boolean checkDuplicateName(String lUserId, String lOrganizationId, String name) {
    Group group = groupRepository.findFirstByNameAndDtimeIsNull(name);
    return (group != null);
  }

  @Override
  public List<Group> selectGroupByUserWithOwn(String lUserId, String lOrganizationId,
      String userId) {

    List<Group> groups = new ArrayList<>();
    List<Group> own = groupRepository.findByUsers_idAndDtimeIsNull(userId);
    List<Group> notOwn = groupRepository.findByUsers_idNotAndDtimeIsNull(userId);

    own = own.stream().map(r -> {
      r.setOwn(true);
      return r;
    }).toList();
    groups.addAll(own);

    notOwn.stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
    groups.addAll(notOwn);

    return groups;
  }



  @Override
  public List<Group> selectGroupByRoleWithOwn(String lUserId, String lOrganizationId,
      String roleId) {

    List<Group> groups = new ArrayList<>();
    Role role = roleRepository.findRoleById(roleId);
    if (role.getGroups() != null && !role.getGroups().isEmpty()) {
      List<String> groupIds =
          role.getGroups().stream().map(u -> u.getId()).collect(Collectors.toList());

      List<Group> own = groupRepository.findByIdInAndDtimeIsNull(groupIds);
      List<Group> notOwn = groupRepository.findByIdNotInAndDtimeIsNull(groupIds);

      own = own.stream().map(r -> {
        r.setOwn(true);
        return r;
      }).toList();
      groups.addAll(own);

      notOwn.stream().map(r -> {
        r.setOwn(false);
        return r;
      }).toList();
      groups.addAll(notOwn);

      return groups;
    } else {

      groups = groupRepository.findByDtimeIsNullOrderByNameAsc().stream().map(r -> {
        r.setOwn(false);
        return r;
      }).collect(Collectors.toList());
      return groups;
    }
  }


  @Override
  public List<Group> selectGroupAllWithOwn(String lUserId, String lOrganizationId) {
    return groupRepository.findByDtimeIsNullOrderByNameAsc().stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
  }



  @Override
  public Page<Group> selectGroupByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    return groupRepository.selectByConditions(lUserId, lOrganizationId, criterias, pageable);
  }


  @Override
  public Page<Group> selectGroupAllWithOwn(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    return this.selectGroupByConditions(lUserId, lOrganizationId, criterias, pageable).map(g -> {
      g.setOwn(false);
      return g;
    });
  }
}
