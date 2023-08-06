package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.User;

public interface UserRepositoryIf {

  public Page<User> selectByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, String loginId, String name, String email,
      String phone, Boolean locked, Pageable pageable);

  public List<User> selectByConditions(String requestUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, String loginId, String name, String email,
      String phone, Boolean locked);

  public List<User> selectByRole_id(String lUserId, String lOrganizationId, String roleId);

  public List<User> selectByRole_name(String lUserId, String lOrganizationId, String name);

  public Page<User> selectByConditions(String lUserId, String lOrganizationId,
      Boolean includeOrganization, String organizationId, List<Criteria> criterias,
      Pageable pageable);

  public Page<User> selectUserByOrganizationWithOwn(String lUserId, String lOrganizationId,
      String organizationId, List<Criteria> criterias, Pageable pageable);
}
