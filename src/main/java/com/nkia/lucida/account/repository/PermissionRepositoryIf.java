package com.nkia.lucida.account.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Permission;

public interface PermissionRepositoryIf {

  public Set<String> selectByRoleIncludeIdFields(List<String> roleIds);

  public Page<Permission> selectByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);
}
