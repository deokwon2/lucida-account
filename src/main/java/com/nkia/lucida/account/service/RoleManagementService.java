package com.nkia.lucida.account.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;

public interface RoleManagementService {

  public Role insertRole(String lUserId, String lOrganizationId, Role role, List<String> userIds,
      List<String> permissionIds, List<String> groupIds);

  public Role updateRole(String lUserId, String lOrganizationId, Role role, List<String> userIds,
      List<String> permissionIds, List<String> groupIds);

  public void deleteRole(String lUserId, String lOrganizationId, boolean ignoreUser,
      List<String> roleIds);

  public Role selectRoleById(String lUserId, String lOrganizationId, String id);

  public List<Role> selectRoleByUser(String lUserId, String lOrganizationId, User user);

  public List<Role> selectRoleByConditions(String lUserId, String lOrganizationId, Role role);

  public List<Role> selectRoleByUserWithOwn(String lUserId, String lOrganizationId, User user);

  public List<Role> selectRoleByGroupWithOwn(String lUserId, String lOrganizationId, Group group);

  public Boolean checkDuplicateName(String lUserId, String lOrganizationId, String name);

  public List<String> selectRoleByUserIncludeIdFileds(String userId);

  public Page<Role> selectRoleByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);
}
