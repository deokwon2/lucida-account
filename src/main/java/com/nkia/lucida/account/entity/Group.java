package com.nkia.lucida.account.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import com.nkia.lucida.common.mongodb.MongoDBIsolationCollection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("group")
@MongoDBIsolationCollection
public class Group {


  @Id
  private String id;
  private String name;
  private String description;
  private Set<GroupUser> users;

  private Long ctime;
  private Long mtime;
  private Long dtime;

  @Transient
  private Boolean own;

  @Builder
  public Group(String id, String name, String description, Set<GroupUser> users, Long ctime,
      Long mtime, Long dtime) {

    this.id = id;
    this.name = name;
    this.description = description;
    this.users = users;
    this.ctime = ctime;
    this.mtime = mtime;
    this.dtime = dtime;
  }



  @Transient
  public void addUser(User user) {
    if (user == null) {
      return;
    }

    long time = Instant.now().getEpochSecond();
    GroupUser organizationUser = GroupUser.builder().id(user.getId()).loginId(user.getLoginId())
        .name(user.getName()).ctime(time).mtime(time).build();
    if (this.users == null) {
      this.users = new HashSet<>();
    }
    this.users.add(organizationUser);
  }



  @Transient
  public void addUser(Collection<User> users) {
    if (users == null) {
      return;
    }

    for (User user : users) {
      this.addUser(user);
    }
  }



  @Transient
  public void excludeUser(User user) {
    if (user == null) {
      return;
    }

    if (this.users == null) {
      return;
    }
    this.users.removeIf(u -> u.getId().equals(user.getId()));
  }



  @Transient
  public void excludeUser(Collection<User> users) {
    if (users == null) {
      return;
    }

    for (User user : users) {
      this.excludeUser(user);
    }
  }



  @Transient
  public void excludeUserAll() {
    if (this.users == null) {
      return;
    }
    this.users.clear();
  }


  @Transient
  public boolean hasUser(String userId) {
    return Optional.ofNullable(this.users).orElseGet(Collections::emptySet).stream()
        .anyMatch(u -> u.getId().equals(userId));
  }
}
