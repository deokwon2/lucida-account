package com.nkia.lucida.account.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.repository.PermissionRepository;
import com.nkia.lucida.account.repository.RoleRepository;

@Service
public class PermissionServiceImpl implements PermissionService {

  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;

  public PermissionServiceImpl(PermissionRepository permissionRepository,
      RoleRepository roleRepository) {
    this.permissionRepository = permissionRepository;
    this.roleRepository = roleRepository;
  }


  @Override
  public Set<Permission> selectPermissionAllWithOwn(String lUserId, String lOrganizationId) {
    List<Permission> permissions = permissionRepository.findAll().stream().map(r -> {
      r.setOwn(false);
      return r;
    }).toList();
    return new HashSet<>(permissions);
  }


  @Override
  public Set<Permission> selectPermissionByRoleWithOwn(String lUserId, String lOrganizationId,
      String roleId) {

    Role role = roleRepository.findRoleById(roleId);
    Set<Permission> includePermission = role.getPermissions();
    includePermission = includePermission == null ? new HashSet<>() : includePermission;
    Set<Permission> allPermission = new HashSet<>(permissionRepository.findAll());

    for (Permission p : allPermission) {
      if (includePermission.contains(p)) {
        p.setOwn(true);
      } else {
        p.setOwn(false);
      }
    }
    return allPermission;
  }



  @Override
  public Set<String> selectPermissionByRoleIncludeIdFields(String lUserId, String lOrganizationId,
      List<String> roleIds) {
    return permissionRepository.selectByRoleIncludeIdFields(roleIds);
  }



  @Override
  public Page<Permission> selectPermissionAllWithOwn(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {


    return this.selectPermissionByConditions(lUserId, lOrganizationId, criterias, pageable)
        .map(u -> {
          u.setOwn(false);
          return u;
        });
  }


  @Override
  public Page<Permission> selectPermissionByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {
    return permissionRepository.selectByConditions(lUserId, lOrganizationId, criterias, pageable);
  }
}
