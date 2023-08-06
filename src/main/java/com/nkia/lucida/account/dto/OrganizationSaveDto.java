package com.nkia.lucida.account.dto;

import java.util.List;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Organization;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationSaveDto {

  private String id;
  private String name;
  private String description;
  private List<String> userIds;


  private String addAdminLoginId;
  private String addAdminName;
  private String addAdminPassword;

  public OrganizationSaveDto() {}

  public OrganizationSaveDto(Organization source) {
    BeanUtils.copyProperties(source, this, "userIds", "addAdminLoginId", "addAdminName",
        "addAdminPassword");
  }

  public OrganizationSaveDto toDto(Organization source) {
    BeanUtils.copyProperties(source, this, "userIds", "addAdminLoginId", "addAdminName",
        "addAdminPassword");
    return this;
  }


  public Organization toEntity() {
    return Organization.builder().id(id).name(name).description(description).build();
  }
}
