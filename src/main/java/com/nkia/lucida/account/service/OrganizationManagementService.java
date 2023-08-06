package com.nkia.lucida.account.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Organization;

public interface OrganizationManagementService {

  public Organization insertOrganization(String lUserId, String lOrganizationId,
      Organization organization, List<String> userIds);

  public Organization insertOrganization(String lUserId, String lOrganizationId,
      Organization organization, List<String> userIds, String addAdminLoginId, String addAdminName,
      String addAdminPassword);


  public Organization updateOrganization(String lUserId, String lOrganizationId,
      Organization organization, List<String> userIds);


  public void deleteOrganization(String lUserId, String lOrganizationId, boolean ignoreUser,
      List<String> organizationIds);


  public Page<Organization> selectOrganizationByConditions(String lUserId, String lOrganizationId,
      Organization organization, Pageable pageable);


  public List<Organization> selectOrganizationByConditions(String lUserId, String lOrganizationId,
      Organization organization);


  public Organization selectOrganizationById(String lUserId, String lOrganizationId,
      String organizationId);


  public Boolean checkDuplicateName(String lUserId, String lOrganizationId, String name);


  public Organization excludeUserFromOrganization(String lUserId, String lOrganizationId,
      String organizationId, List<String> userIds);


  public Organization addUserFromOrganization(String lUserId, String lOrganizationId,
      String organizationId, List<String> userIds);

  public Page<Organization> selectOrganizationByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);
}
