package com.nkia.lucida.account.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.common.mongodb.GridPaginationHelper;
import com.nkia.lucida.common.mongodb.TenantMongoDBConstants;

@Repository
public class OrganizationRepositoryImpl implements OrganizationRepositoryIf {


  private final MongoTemplate mongoTemplateShared;


  public OrganizationRepositoryImpl(
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_ISOLATION) MongoTemplate mongoTemplateIsolation,
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_SHARED) MongoTemplate mongoTemplateShared) {
    this.mongoTemplateShared = mongoTemplateShared;
  }


  @Override
  public Page<Organization> selectByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    List<Criteria> criteriaItems = new ArrayList<>();
    criteriaItems.addAll(criterias);
    criteriaItems.add(Criteria.where("dtime").isNull());

    Query query = new Query().with(pageable);
    query.addCriteria(Criteria.where("").andOperator(criteriaItems));

    return GridPaginationHelper.INSTANCE.pageFind(mongoTemplateShared, pageable, criterias,
        "organization", Organization.class);
  }
}
