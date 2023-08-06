package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.common.mongodb.MongoDBIsolationCollection;

/**
 * @author henoh@nkia.co.kr on 2023-01-17
 */
@MongoDBIsolationCollection
public interface RoleRepository extends MongoRepository<Role, String>, RoleRepositoryIf {

  public Role findRoleById(String id);

  public List<Role> findByUsers_idAndDtimeIsNull(String userId);

  public List<Role> findByUsers_idNotAndDtimeIsNull(String userId);

  public List<Role> findByIdInAndDtimeIsNull(List<String> ids);

  public List<Role> findAllByPermissionsIsContaining(Permission permission);

  @Query(value = "{ 'users.loginId' : ?0, dtime: null }", fields = "{ 'id': 1 }")
  public List<Role> selectByUsers_loginIdIncludeIdFields(String userLoginId);

  @Query(value = "{ 'users.id' : ?0, dtime: null }", fields = "{ 'id': 1 }")
  public List<Role> selectByUsers_idIncludeIdFields(String userId);

  public Role findByIdAndUsers_id(String id, String userId);

  public Role findByIdAndGroups_id(String id, String groupId);

  public List<Role> findByGroups_idAndDtimeIsNull(String groupId);

  public List<Role> findByGroups_idNotAndDtimeIsNull(String groupId);

  public List<Role> findByDtimeIsNullOrderByNameAsc();

  public Role findFirstByNameAndDtimeIsNull(String roleName);

  public List<Role> findByPermissions_idInAndDtimeIsNull(List<String> permissionIds);

  public List<Role> findByPermissionsInAndDtimeIsNull(List<Permission> permissions);
}
