package com.nkia.lucida.account.dto;

import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.common.mongodb.GridPageableDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoPageableDto extends GridPageableDto {

  private String organizationId;
  private String id;
  private String loginId;
  private String name;
  private String email;
  private String phone;
  private Boolean locked;
  private Boolean needChangePassword;
  private Long lastLoginTime;

  public User toEntity() {
    return User.builder().id(id).loginId(loginId).name(name).email(email).phone(phone)
        .locked(locked).needChangePassword(needChangePassword).lastLoginTime(lastLoginTime).build();
  }
}
