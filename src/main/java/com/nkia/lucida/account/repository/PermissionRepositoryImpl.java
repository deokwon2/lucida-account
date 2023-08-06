package com.nkia.lucida.account.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.common.mongodb.GridPaginationHelper;
import com.nkia.lucida.common.mongodb.TenantMongoDBConstants;

@Repository
public class PermissionRepositoryImpl implements PermissionRepositoryIf {

  private final MongoTemplate mongoTemplateShared;
  private final MongoTemplate mongoTemplateIsolation;


  public PermissionRepositoryImpl(
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_ISOLATION) MongoTemplate mongoTemplateIsolation,
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_SHARED) MongoTemplate mongoTemplateShared) {
    this.mongoTemplateShared = mongoTemplateShared;
    this.mongoTemplateIsolation = mongoTemplateIsolation;
  }


  @Override
  public Set<String> selectByRoleIncludeIdFields(List<String> roleIds) {

    List<Criteria> criterias = new ArrayList<>();
    criterias.add(Criteria.where("dtime").isNull());
    criterias.add(Criteria.where("id").in(roleIds));

    List<Order> orders = new ArrayList<>();
    orders.add(Sort.Order.asc("id"));
    orders.add(Sort.Order.asc("name"));

    Query query = new Query();
    query.addCriteria(Criteria.where("").andOperator(criterias));
    query.with(Sort.by(orders));

    List<Role> roles = mongoTemplateIsolation.find(query, Role.class);
    Set<String> permission = roles.stream().map(r -> {

      // return r.getPermissions().stream().map(p -> p.getId()).collect(Collectors.toSet());

      return r.getPermissions().stream().filter(Objects::nonNull).map(p -> p.getId())
          .collect(Collectors.toSet());

    }).flatMap(id -> id.stream()).collect(Collectors.toSet());

    return permission;
  }



  @Override
  public Page<Permission> selectByConditions(String lUserId, String lOrganizationId,
      List<Criteria> criterias, Pageable pageable) {

    Query query = new Query().with(pageable);
    if (criterias != null && !criterias.isEmpty()) {
      query.addCriteria(Criteria.where("").andOperator(criterias));
    }

    return GridPaginationHelper.INSTANCE.pageFind(mongoTemplateShared, pageable, criterias,
        "permission", Permission.class);
  }
}
