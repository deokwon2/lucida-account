package com.nkia.lucida.account.dto;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupInfoDto implements Comparable<GroupInfoDto> {

  private long index;
  private String id;
  private String name;
  private String description;
  private Boolean own;

  private TreeSet<UserInfoDto> users = new TreeSet<>();
  private TreeSet<RoleInfoDto> roles = new TreeSet<>();
  private TreeSet<String> displayUsers = new TreeSet<>();


  public GroupInfoDto() {}

  public GroupInfoDto(Group source) {
    BeanUtils.copyProperties(source, this);
  }

  public GroupInfoDto(Group source, Collection<User> users, Collection<Role> roles,
      boolean useOwn) {
    BeanUtils.copyProperties(source, this);
    if (users != null) {
      for (User user : users) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.toDto(user);
        this.users.add(userInfoDto);

        if (useOwn && user.getOwn()) {
          displayUsers.add(user.getLoginId() + "(" + user.getName() + ")");
        }
      }
    }

    if (roles != null) {
      for (Role role : roles) {
        RoleInfoDto roleInfoDto = new RoleInfoDto();
        roleInfoDto.toDto(role);
        this.roles.add(roleInfoDto);
      }
    }
  }


  public GroupInfoDto(Group source, Collection<User> users) {
    BeanUtils.copyProperties(source, this);
    if (users != null) {
      for (User user : users) {
        displayUsers.add(user.getLoginId() + "(" + user.getName() + ")");
      }
    }
  }


  public GroupInfoDto toDto(Group source) {
    BeanUtils.copyProperties(source, this);
    return this;
  }


  public Group toEntity() {
    long time = Instant.now().getEpochSecond();
    return Group.builder().id(id).name(name).description(description).ctime(time).mtime(time)
        .build();
  }



  @SuppressWarnings("unused")
  private int compare(GroupInfoDto o1, GroupInfoDto o2) {
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
  public int compareTo(GroupInfoDto o) {
    // return Comparator.comparing((GroupInfoDto s) -> s.name).compare(this, o);
    return Comparator
        .comparing(GroupInfoDto::getName, Comparator.nullsFirst(Comparator.naturalOrder()))
        .compare(this, o);
  }
}
