package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.common.mongodb.MongoDBSharedCollection;

@MongoDBSharedCollection
public interface UserRepository extends MongoRepository<User, String>, UserRepositoryIf {

  public User findUserById(String id);

  public User findUserByEmailAndDtimeIsNull(String email);

  public User findUserByLoginIdAndDtimeIsNull(String loginId);

  public List<User> findByNameContainingAndDtimeIsNull(String name);

  public List<User> findByNameStartingWithAndDtimeIsNull(String name);

  public List<User> findByNameEndingWithAndDtimeIsNull(String name);

  public List<User> findByDtimeIsNull();

  public List<User> findByIdInAndOrganizations_idAndDtimeIsNull(List<String> ids,
      String organizationId);

  public List<User> findByIdNotInAndOrganizations_idAndDtimeIsNull(List<String> ids,
      String organizationId);

  public List<User> findByOrganizations_idNotAndDtimeIsNull(String organizationId);

  public User findByIdAndOrganizations_id(String id, String organizationId);

  public List<User> findByOrganizations_idAndDtimeIsNull(String organizationId);

  public List<User> findByIdInAndDtimeIsNull(List<String> ids);
}
