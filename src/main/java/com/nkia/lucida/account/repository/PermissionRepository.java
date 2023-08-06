package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.common.mongodb.MongoDBSharedCollection;


@MongoDBSharedCollection
public interface PermissionRepository
    extends MongoRepository<Permission, String>, PermissionRepositoryIf {

  public List<Permission> findByIdIn(List<String> ids);

}
