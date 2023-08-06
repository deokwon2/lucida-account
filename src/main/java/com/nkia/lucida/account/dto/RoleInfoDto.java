package com.nkia.lucida.account.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Permission;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleInfoDto implements Comparable<RoleInfoDto> {

  private long index;
  private String id;
  private String name;
  private String description;
  private Boolean own;

  private TreeSet<UserInfoDto> users = new TreeSet<>();
  private TreeSet<PermissionInfoDto> permissions = new TreeSet<>();
  private TreeSet<GroupInfoDto> groups = new TreeSet<>();

  private int userAmount = 0;
  private int permissionAmount = 0;
  private int groupAmount = 0;

  public RoleInfoDto() {}

  public RoleInfoDto(Role source) {
    BeanUtils.copyProperties(source, this);

    if (source.getUsers() != null) {
      this.setUserAmount(source.getUsers().size());
    }

    if (source.getPermissions() != null) {
      this.setPermissionAmount(source.getPermissions().size());
    }

    if (source.getGroups() != null) {
      this.setGroupAmount(source.getGroups().size());
    }
  }

  public RoleInfoDto(Role source, Collection<User> users, Collection<Permission> permissions,
      Collection<Group> groups) {
    BeanUtils.copyProperties(source, this);
    if (users != null) {
      for (User user : users) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.toDto(user);
        this.users.add(userInfoDto);
      }
    }

    if (permissions != null) {
      for (Permission permission : permissions) {
        PermissionInfoDto permissionInfoDto = new PermissionInfoDto();
        permissionInfoDto.toDto(permission);
        this.permissions.add(permissionInfoDto);
      }
    }

    if (groups != null) {
      for (Group group : groups) {
        GroupInfoDto groupInfoDto = new GroupInfoDto();
        groupInfoDto.toDto(group);
        this.groups.add(groupInfoDto);
      }
    }
  }


  public RoleInfoDto toDto(Role source) {
    BeanUtils.copyProperties(source, this);
    return this;
  }


  public Role toEntity() {
    return null;
  }



  @SuppressWarnings("unused")
  private int compare(RoleInfoDto o1, RoleInfoDto o2) {
    if (o1.getName() == o2.getName()) {
      // Nulls or exact equality
      return 0;
    } else if (o1.getName() == null) {
      // s1 null and s2 not null, so s1 greater
      return 1;
    } else if (o2.getName() == null) {
      // s2 null and s1 not null, so s1 less
      return -1;
    } else {
      return o2.getName().compareTo(o1.getName());
    }
  }


  @Override
  public int compareTo(RoleInfoDto o) {
    // return Comparator.comparing((RoleInfoDto s) -> s.name).compare(this, o);
    return Comparator
        .comparing(RoleInfoDto::getName, Comparator.nullsFirst(Comparator.naturalOrder()))
        .compare(this, o);
  }
}
