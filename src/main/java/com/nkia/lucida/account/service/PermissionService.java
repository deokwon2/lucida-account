package com.nkia.lucida.account.service;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Permission;

public interface PermissionService {

  public Set<Permission> selectPermissionAllWithOwn(String lUserId, String lOrganizationId);

  public Set<Permission> selectPermissionByRoleWithOwn(String lUserId, String lOrganizationId,
      String roleId);

  public Set<String> selectPermissionByRoleIncludeIdFields(String lUserId, String lOrganizationId,
      List<String> roleIds);

  public Page<Permission> selectPermissionAllWithOwn(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);

  public Page<Permission> selectPermissionByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);
}
