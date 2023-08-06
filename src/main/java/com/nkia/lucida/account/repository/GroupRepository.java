package com.nkia.lucida.account.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.common.mongodb.MongoDBIsolationCollection;

@MongoDBIsolationCollection
public interface GroupRepository extends MongoRepository<Group, String>, GroupRepositoryIf {

  public Group findGroupById(String id);

  public Group findFirstByNameAndDtimeIsNull(String name);

  public Group findByIdAndUsers_id(String id, String userId);

  public List<Group> findByIdInAndDtimeIsNull(List<String> groupIds);

  public List<Group> findByIdNotInAndDtimeIsNull(List<String> groupIds);

  public List<Group> findByDtimeIsNullOrderByNameAsc();

  public List<Group> findByUsers_idAndDtimeIsNull(String userId);

  public List<Group> findByUsers_idNotAndDtimeIsNull(String userId);
}
