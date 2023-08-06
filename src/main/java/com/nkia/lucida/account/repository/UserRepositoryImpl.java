package com.nkia.lucida.account.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.common.mongodb.GridPaginationHelper;
import com.nkia.lucida.common.mongodb.TenantMongoDBConstants;


@Repository
public class UserRepositoryImpl implements UserRepositoryIf {


  private final MongoTemplate mongoTemplateIsolation;
  private final MongoTemplate mongoTemplateShared;


  public UserRepositoryImpl(
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_ISOLATION) MongoTemplate mongoTemplateIsolation,
      @Qualifier(TenantMongoDBConstants.MONGODB_TEMPLATE_SHARED) MongoTemplate mongoTemplateShared) {
    this.mongoTemplateIsolation = mongoTemplateIsolation;
    this.mongoTemplateShared = mongoTemplateShared;
  }



  @Override
  public Page<User> selectByConditions(String requestUserId, String requestOrganizationId,
      Boolean includeOrganization, String organizationId, String loginId, String name, String email,
      String phone, Boolean locked, Pageable pageable) {

    List<Criteria> criterias = new ArrayList<>();
    criterias.add(Criteria.where("dtime").isNull());
    criterias.addAll(createConditions(requestUserId, requestOrganizationId, includeOrganization,
        organizationId, loginId, name, email, phone, locked));

    Query query = new Query().with(pageable);
    if (!criterias.isEmpty()) {
      query.addCriteria(Criteria.where("").andOperator(criterias));
    }

    List<User> users = mongoTemplateShared.find(query, User.class);
    Page<User> usersPage = PageableExecutionUtils.getPage(users, pageable,
        () -> mongoTemplateShared.count(query.skip(-1).limit(-1), User.class));
    return usersPage;

  }


  @Override
  public List<User> selectByConditions(String requestUserId, String requestOrganizationId,
      Boolean includeOrganization, String organizationId, String loginId, String name, String email,
      String phone, Boolean locked) {

    List<Criteria> criterias = new ArrayList<>();
    criterias.add(Criteria.where("dtime").isNull());
    criterias.addAll(createConditions(requestUserId, requestOrganizationId, includeOrganization,
        organizationId, loginId, name, email, phone, locked));

    Query query = new Query();
    if (!criterias.isEmpty()) {
      query.addCriteria(Criteria.where("").andOperator(criterias));
    }
    return mongoTemplateShared.find(query, User.class);
  }



  private List<Criteria> createConditions(String requestUserId, String requestOrganizationId,
      Boolean includeOrganization, String organizationId, String loginId, String name, String email,
      String phone, Boolean locked) {

    List<Criteria> criterias = new ArrayList<>();
    if (!includeOrganization) {
      criterias.add(Criteria.where("organizations.id").ne(organizationId));
    } else {
      criterias.add(Criteria.where("organizations.id").is(organizationId));
    }

    if (loginId != null && !loginId.isEmpty()) {
      criterias.add(Criteria.where("loginId").regex(loginId, "i"));
    }
    if (name != null && !name.isEmpty()) {
      criterias.add(Criteria.where("name").regex(name, "i"));
    }
    if (email != null && !email.isEmpty()) {
      criterias.add(Criteria.where("email").regex(email, "i"));
    }
    if (phone != null && !phone.isEmpty()) {
      criterias.add(Criteria.where("phone").regex(phone, "i"));
    }
    if (locked != null) {
      criterias.add(Criteria.where("locked").is(locked));
    }
    return criterias;
  }


  @Override
  public List<User> selectByRole_id(String requestUserId, String requestOrganizationId,
      String roleId) {

    Query roleQuery = new Query();
    roleQuery.addCriteria(Criteria.where("id").is(roleId));
    List<Role> roles = mongoTemplateIsolation.find(roleQuery, Role.class);

    if (roles == null) {
      return new ArrayList<>();
    }

    List<String> roleUsers = roles.stream().map(r -> {

      return r.getUsers().stream().map(u -> u.getId()).collect(Collectors.toList());

    }).flatMap(u -> u.stream()).collect(Collectors.toList());

    Query userQuery = new Query();
    userQuery.addCriteria(Criteria.where("id").in(roleUsers));



    return mongoTemplateShared.find(userQuery, User.class);

  }


  @Override
  public List<User> selectByRole_name(String requestUserId, String requestOrganizationId,
      String name) {

    Query roleQuery = new Query();
    roleQuery.addCriteria(Criteria.where("name").is(name));
    List<Role> roles = mongoTemplateIsolation.find(roleQuery, Role.class);

    if (roles == null) {
      return new ArrayList<>();
    }

    List<String> roleUsers = roles.stream().map(r -> {

      return r.getUsers().stream().map(u -> u.getId()).collect(Collectors.toList());

    }).flatMap(u -> u.stream()).collect(Collectors.toList());

    Query userQuery = new Query();
    userQuery.addCriteria(Criteria.where("id").in(roleUsers));
    return mongoTemplateShared.find(userQuery, User.class);

  }



  @Override
  public Page<User> selectByConditions(String requestUserId, String requestOrganizationId,
      Boolean includeOrganization, String organizationId, List<Criteria> criterias,
      Pageable pageable) {

    List<Criteria> criteriaItems = new ArrayList<>();
    criteriaItems.addAll(criterias);
    criteriaItems.add(Criteria.where("dtime").isNull());

    if (!includeOrganization) {
      criteriaItems.add(Criteria.where("organizations.id").ne(organizationId));
    } else {
      criteriaItems.add(Criteria.where("organizations.id").is(organizationId));
    }



    Query query = new Query().with(pageable);
    query.addCriteria(Criteria.where("").andOperator(criteriaItems));

    List<User> users = mongoTemplateShared.find(query, User.class);
    Page<User> usersPage = PageableExecutionUtils.getPage(users, pageable,
        () -> mongoTemplateShared.count(query.skip(-1).limit(-1), User.class));

    return usersPage;
  }



  @Override
  public Page<User> selectUserByOrganizationWithOwn(String lUserId, String lOrganizationId,
      String organizationId, List<Criteria> criterias, Pageable pageable) {

    List<AggregationOperation> aOperations = new ArrayList<>();

    Fields fields = Fields.fields("id", "loginId", "name", "own");

    ProjectionOperation po = Aggregation.project().andInclude(fields)
        .and(ConditionalOperators.when(
            ArrayOperators.arrayOf("organizations._id").containsValue(new ObjectId(organizationId)))
            .then(true).otherwise(false))
        .as("own");

    aOperations.add(po);

    return GridPaginationHelper.INSTANCE.pageAggregation(mongoTemplateShared, pageable, aOperations,
        "user", User.class);
  }
}
