package com.nkia.lucida.account.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.User;

public interface UserManagementService {

  public User insertUser(String lUserId, String lOrganizationId, User user, List<String> roleIds,
      List<String> groupIds);


  public void insertUser(String lUserId, String lOrganizationId, List<User> users,
      List<String> roleIds);


  public User updateUser(String lUserId, String lOrganizationId, User user, List<String> roleIds,
      List<String> groupIds);


  public User updateUserPassword(String lUserId, String lOrganizationId, String userId,
      String currentPassword, String newPassword);


  public User updateUserPasswordByAdmin(String lUserId, String lOrganizationId, String userId,
      String newPassword);


  public void deleteUser(String lUserId, String lOrganizationId, boolean ignoreRole,
      List<String> id);


  public Page<User> selectUserByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, User user, Pageable pageable);


  public List<User> selectUserByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, User user);


  public User selectUserById(String lUserId, String lOrganizationId, String id);

  public User selectUserByLoginId(String lUserId, String lOrganizationId, String loginId);

  public List<User> selectUserByOrganizationWithOwn(String lUserId, String lOrganizationId,
      String organizationId);

  public Boolean checkDuplicateLoginId(String lUserId, String lOrganizationId, String loginId);

  public List<User> selectUserByRoleWithOwn(String lUserId, String lOrganizationId, String roleId);

  public List<User> selectUserByGroupWithOwn(String lUserId, String lOrganizationId,
      String groupId);

  public List<User> selectUserByGroup(String lUserId, String lOrganizationId, String groupId);


  public List<User> selectUserAllWithOwn(String lUserId, String lOrganizationId);


  public Page<User> selectUserByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, List<Criteria> criterias,
      Pageable pageable);


  public Page<User> selectUserAllWithOwn(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);


  public Page<User> selectUserByOrganizationWithOwn(String lUserId, String lOrganizationId,
      String organizationId, List<Criteria> criterias, Pageable pageable);

  public List<User> selectUserAll(String lUserId, String lOrganizationId);
}
