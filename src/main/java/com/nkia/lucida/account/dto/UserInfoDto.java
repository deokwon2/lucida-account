package com.nkia.lucida.account.dto;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Group;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.common.dto.GridColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class UserInfoDto implements Comparable<UserInfoDto> {
  private static final String LOCALE_DELIM = "-";

  private long index;

  @GridColumn(visible = false)
  private String id;

  private String loginId;

  private String name;

  private String email;

  private String phone;

  private Boolean needChangePassword;

  private Boolean locked;

  private String description;

  @GridColumn(exclude = true)
  private String locale;

  private Long ctime;

  private Long mtime;

  @GridColumn(exclude = true)
  private Long dtime;

  private Long lastLoginTime;

  @GridColumn(exclude = true)
  private TreeSet<RoleInfoDto> roles = new TreeSet<>();

  @GridColumn(exclude = true)
  private TreeSet<GroupInfoDto> groups = new TreeSet<>();

  @GridColumn(displayKey = "check-box")
  private Boolean own;

  public UserInfoDto() {}

  public UserInfoDto(User source) {
    BeanUtils.copyProperties(source, this);
  }

  public UserInfoDto(User source, Boolean own) {
    BeanUtils.copyProperties(source, this);
    this.own = own;
  }



  public UserInfoDto(User source, Collection<Role> roles, Collection<Group> groups) {
    BeanUtils.copyProperties(source, this);
    if (roles != null) {
      for (Role role : roles) {
        RoleInfoDto roleInfoDto = new RoleInfoDto();
        roleInfoDto.toDto(role);
        this.roles.add(roleInfoDto);
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



  public UserInfoDto toDto(User source) {
    BeanUtils.copyProperties(source, this);
    return this;
  }



  public UserInfoDto toDto(User source, Collection<Role> roles, Collection<Group> groups) {
    BeanUtils.copyProperties(source, this);
    if (roles != null) {
      for (Role role : roles) {
        RoleInfoDto roleInfoDto = new RoleInfoDto();
        roleInfoDto.toDto(role);
        this.roles.add(roleInfoDto);
      }
    }

    if (groups != null) {
      for (Group group : groups) {
        GroupInfoDto groupInfoDto = new GroupInfoDto();
        groupInfoDto.toDto(group);
        this.groups.add(groupInfoDto);
      }
    }
    return this;
  }



  public User toEntity() {

    User user = User.builder().id(id).loginId(loginId).name(name).email(email).phone(phone)
        .locked(locked).needChangePassword(needChangePassword).description(description).build();

    Locale userLocale = null;
    if (locale != null && !locale.isEmpty()) {
      String[] locales = locale.split(LOCALE_DELIM);
      userLocale = locales.length > 1 ? new Locale(locales[0], locales[1]) : new Locale(locales[0]);
    }
    user.setLocale(userLocale);

    return user;
  }


  @SuppressWarnings("unused")
  private int compare(UserInfoDto o1, UserInfoDto o2) {
    if (o1.getLoginId() == o2.getLoginId()) {
      // Nulls or exact equality
      return 0;
    } else if (o1.getLoginId() == null) {
      // s1 null and s2 not null, so s1 greater
      return 1;
    } else if (o2.getLoginId() == null) {
      // s2 null and s1 not null, so s1 less
      return -1;
    } else {
      return o2.getLoginId().compareTo(o1.getLoginId());
    }
  }

  @Override
  public int compareTo(UserInfoDto o) {
    // return Comparator.comparing((UserInfoDto s) -> s.loginId).thenComparing(s -> s.name)
    // .compare(this, o);
    return Comparator
        .comparing(UserInfoDto::getLoginId, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(UserInfoDto::getName, Comparator.nullsFirst(Comparator.naturalOrder()))
        .compare(this, o);
  }
}
