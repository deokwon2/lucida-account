package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.common.mongodb.MongoDBIsolationCollection;

@MongoDBIsolationCollection
public interface GroupRepositoryIf {

  public Page<Group> selectByConditions(String requestUserId, String requestOrganizationId,
      List<Criteria> criterias, Pageable pageable);
}
