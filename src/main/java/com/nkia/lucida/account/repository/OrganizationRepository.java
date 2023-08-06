package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.common.mongodb.MongoDBSharedCollection;

@MongoDBSharedCollection
public interface OrganizationRepository
    extends MongoRepository<Organization, String>, OrganizationRepositoryIf {

  public Organization findFirstByNameAndDtimeIsNull(String name);

  public List<Organization> findByDtimeIsNullOrderByNameAsc();

  public Organization findOrganizationById(String Id);

  public List<Organization> findByUsers_idAndDtimeIsNull(String userId);

  public List<Organization> findByUsers_idNotAndDtimeIsNull(String userId);

  public List<Organization> findByUsers_loginIdAndDtimeIsNull(String loginId);

  public List<Organization> findByUsers_loginIdNotAndDtimeIsNull(String loginId);

  public Long countByUsers_loginIdAndDtimeIsNull(String loginId);

  public Organization findByIdAndUsers_id(String id, String userId);

  @Query(value = "{ 'dtime' : null } ", fields = "{'_id' : 1}")
  public List<Organization> selectByDtimeIsNullIncludeIdFields();
}
