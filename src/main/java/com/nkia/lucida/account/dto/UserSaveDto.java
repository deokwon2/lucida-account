package com.nkia.lucida.account.dto;

import java.util.List;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSaveDto {

  private String id;
  private String loginId;
  private String name;
  private String password;
  private String email;
  private String phone;
  private String description;
  private Boolean locked;
  private Boolean needChangePassword;
  private Long lastLoginTime;
  private List<String> roleIds;
  private List<String> groupIds;


  public UserSaveDto() {}

  public UserSaveDto(User source) {
    BeanUtils.copyProperties(source, this, "roleIds", "groupIds");
  }

  public UserSaveDto toDto(User source) {
    BeanUtils.copyProperties(source, this, "roleIds", "groupIds");
    return this;
  }


  public User toEntity() {
    return User.builder().id(id).loginId(loginId).name(name).password(password).email(email)
        .phone(phone).locked(locked).needChangePassword(needChangePassword)
        .lastLoginTime(lastLoginTime).description(description).build();
  }
}
