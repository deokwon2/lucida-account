package com.nkia.lucida.account.dto;

import com.nkia.lucida.account.entity.Organization;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationInfoPageableDto {

  private String id;
  private String name;
  private String description;

  public Organization toEntity() {
    return Organization.builder().id(id).name(name).description(description).build();
  }

}
