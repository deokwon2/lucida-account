package com.nkia.lucida.account.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.common.mongodb.TenantMongoDBConstants;


@Repository
public class GroupRepositoryImpl implements GroupRepositoryIf {


  private final MongoTemplate mongoTemplateIsolation;


  public GroupRepositoryImpl(
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_ISOLATION) MongoTemplate mongoTemplateIsolation,
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_SHARED) MongoTemplate mongoTemplateShared) {
    this.mongoTemplateIsolation = mongoTemplateIsolation;
  }



  @Override
  public Page<Group> selectByConditions(String requestUserId, String requestOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    List<Criteria> criteriaItems = new ArrayList<>();
    criteriaItems.addAll(criterias);
    criteriaItems.add(Criteria.where("dtime").isNull());


    Query query = new Query().with(pageable);
    query.addCriteria(Criteria.where("").andOperator(criteriaItems));

    List<Group> items = mongoTemplateIsolation.find(query, Group.class);
    Page<Group> pageItems = PageableExecutionUtils.getPage(items, pageable,
        () -> mongoTemplateIsolation.count(query.skip(-1).limit(-1), Group.class));
    return pageItems;
  }
}
