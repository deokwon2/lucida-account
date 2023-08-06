package com.nkia.lucida.account.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Group;

public interface GroupManagementService {

  public Group insertGroup(String lUserId, String lOrganizationId, Group group,
      List<String> userIds, List<String> roleIds);


  public Group updateGroup(String lUserId, String lOrganizationId, Group group,
      List<String> userIds, List<String> roleIds);


  public void deleteGroup(String lUserId, String lOrganizationId, boolean ignoreUser,
      List<String> groupIds);


  public Page<Group> selectGroupByConditions(String lUserId, String lOrganizationId, Group group,
      Pageable pageable);


  public List<Group> selectGroupByConditions(String lUserId, String lOrganizationId, Group group);


  public List<Group> selectGroupByUserWithOwn(String lUserId, String lOrganizationId,
      String userId);


  public List<Group> selectGroupByRoleWithOwn(String lUserId, String lOrganizationId,
      String roleId);


  public Group selectGroupById(String lUserId, String lOrganizationId, String groupId);


  public Boolean checkDuplicateName(String lUserId, String lOrganizationId, String name);


  public List<Group> selectGroupAllWithOwn(String lUserId, String lOrganizationId);


  public Page<Group> selectGroupByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);


  public Page<Group> selectGroupAllWithOwn(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);
}
