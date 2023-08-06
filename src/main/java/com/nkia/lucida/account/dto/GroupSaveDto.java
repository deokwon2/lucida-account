package com.nkia.lucida.account.dto;

import java.util.List;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Group;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupSaveDto {

  private String id;
  private String name;
  private String description;
  private List<String> userIds;
  private List<String> roleIds;

  public GroupSaveDto() {}

  public GroupSaveDto(Group source) {
    BeanUtils.copyProperties(source, this, "userIds", "roleIds");
  }

  public GroupSaveDto toDto(Group source) {
    BeanUtils.copyProperties(source, this, "userIds", "roleIds");
    return this;
  }


  public Group toEntity() {
    return Group.builder().id(id).name(name).description(description).build();
  }
}
