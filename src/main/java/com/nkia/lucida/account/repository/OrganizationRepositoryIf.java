package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.common.mongodb.MongoDBSharedCollection;

@MongoDBSharedCollection
public interface OrganizationRepositoryIf {


  public Page<Organization> selectByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable);

}
