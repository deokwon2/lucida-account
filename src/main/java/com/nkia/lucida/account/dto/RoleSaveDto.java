package com.nkia.lucida.account.dto;

import java.util.List;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleSaveDto {

  private String id;
  private String name;
  private String description;
  private List<String> userIds;
  private List<String> permissionIds;
  private List<String> groupIds;

  public RoleSaveDto() {}

  public RoleSaveDto(Role source) {
    BeanUtils.copyProperties(source, this, "userIds", "permissionIds", "groupIds");
  }

  public RoleSaveDto toDto(Role source) {
    BeanUtils.copyProperties(source, this, "userIds", "permissionIds", "groupIds");
    return this;
  }


  public Role toEntity() {
    return Role.builder().id(id).name(name).description(description).build();
  }
}
