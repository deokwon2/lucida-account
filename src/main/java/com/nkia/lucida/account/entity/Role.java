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
@Document(collection = "role")
@MongoDBIsolationCollection
public class Role {

  @Id
  private String id;
  private String name;
  private String description;
  private String roleType;
  private Long ctime;
  private Long mtime;
  private Long dtime;

  private Set<RoleGroup> groups;
  private Set<RoleUser> users;
  private Set<Permission> permissions;


  @Transient
  private Boolean own;

  @Transient
  private int userAmount = 0;

  @Transient
  private int permissionAmount = 0;

  @Transient
  private int groupAmount = 0;


  @Builder
  public Role(String id, String name, String description, Long ctime, Long mtime, Long dtime,
      Set<RoleGroup> groups, Set<RoleUser> users, Set<Permission> permissions) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.ctime = ctime;
    this.mtime = mtime;
    this.dtime = dtime;
    this.groups = groups;
    this.users = users;
    this.permissions = permissions;
  }


  @Transient
  public void addUser(User user) {
    if (user == null) {
      return;
    }

    if (this.users == null) {
      this.users = new HashSet<>();
    }

    long time = Instant.now().getEpochSecond();
    RoleUser roleUser = RoleUser.builder().id(user.getId()).loginId(user.getLoginId())
        .name(user.getName()).ctime(time).mtime(time).build();
    this.users.add(roleUser);
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
    this.users.removeIf(o -> o.getId().equals(user.getId()));
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
  public void addPermission(Permission permission) {
    if (permission == null) {
      return;
    }

    if (this.permissions == null) {
      this.permissions = new HashSet<>();
    }
    this.permissions.add(permission);
  }



  @Transient
  public void addPermission(Collection<Permission> permissions) {
    if (permissions == null) {
      return;
    }

    for (Permission permission : permissions) {
      this.addPermission(permission);
    }
  }



  @Transient
  public void excludePermission(Permission permission) {
    if (this.permissions == null) {
      return;
    }
    this.permissions.removeIf(o -> o.getId().equals(permission.getId()));
  }



  @Transient
  public void excludePermission(Collection<Permission> permissions) {
    if (permissions == null) {
      return;
    }

    for (Permission permission : permissions) {
      this.excludePermission(permission);
    }
  }



  @Transient
  public void excludePermissionAll() {
    if (this.permissions == null) {
      return;
    }
    this.permissions.clear();
  }



  @Transient
  public void addGroup(Group group) {
    if (group == null) {
      return;
    }

    if (this.groups == null) {
      this.groups = new HashSet<>();
    }

    long time = Instant.now().getEpochSecond();
    RoleGroup roleGroup = RoleGroup.builder().id(group.getId()).name(group.getName())
        .description(group.getDescription()).ctime(time).mtime(time).build();
    this.groups.add(roleGroup);
  }



  @Transient
  public void addGroup(Collection<Group> groups) {
    if (groups == null) {
      return;
    }

    for (Group group : groups) {
      this.addGroup(group);
    }
  }



  @Transient
  public void excludeGroup(Group group) {
    if (group == null) {
      return;
    }

    if (this.groups == null) {
      return;
    }
    this.groups.removeIf(o -> o.getId().equals(group.getId()));
  }


  @Transient
  public void excludeGroup(Collection<Group> groups) {
    if (groups == null) {
      return;
    }

    for (Group group : groups) {
      this.excludeGroup(group);
    }
  }



  @Transient
  public void excludeGroupAll() {
    if (this.groups == null) {
      return;
    }
    this.groups.clear();
  }



  @Transient
  public boolean hasGroup(String groupId) {
    return Optional.ofNullable(this.groups).orElseGet(Collections::emptySet).stream()
        .anyMatch(u -> u.getId().equals(groupId));
  }


  @Transient
  public boolean hasUser(String userId) {
    return Optional.ofNullable(this.users).orElseGet(Collections::emptySet).stream()
        .anyMatch(u -> u.getId().equals(userId));
  }

  @Transient
  public boolean hasPermission(String permissionId) {
    return Optional.ofNullable(this.permissions).orElseGet(Collections::emptySet).stream()
        .anyMatch(p -> p.getId().equals(permissionId));
  }
}

