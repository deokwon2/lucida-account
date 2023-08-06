package com.nkia.lucida.account.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.common.mongodb.GridPaginationHelper;
import com.nkia.lucida.common.mongodb.TenantMongoDBConstants;


@Repository
public class RoleRepositoryImpl implements RoleRepositoryIf {


  private final MongoTemplate mongoTemplateIsolation;


  public RoleRepositoryImpl(
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_ISOLATION) MongoTemplate mongoTemplateIsolation,
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_SHARED) MongoTemplate mongoTemplateShared) {
    this.mongoTemplateIsolation = mongoTemplateIsolation;
  }



  @Override
  public Page<Role> selectByConditions(String requestUserId, String requestOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    List<Criteria> criteriaItems = new ArrayList<>();
    criteriaItems.addAll(criterias);
    criteriaItems.add(Criteria.where("dtime").isNull());


    // Query query = new Query().with(pageable);
    // query.addCriteria(Criteria.where("").andOperator(criteriaItems));
    //
    // List<Role> items = mongoTemplateIsolation.find(query, Role.class);
    // Page<Role> pateItems = PageableExecutionUtils.getPage(items, pageable,
    // () -> mongoTemplateShared.count(query.skip(-1).limit(-1), Role.class));
    //
    // return pateItems;

    return GridPaginationHelper.INSTANCE.pageFind(mongoTemplateIsolation, pageable, criteriaItems,
        "role", Role.class);
  }
}
