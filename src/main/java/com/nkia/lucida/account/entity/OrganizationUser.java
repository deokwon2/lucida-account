package com.nkia.lucida.account.entity;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrganizationUser {
  String id;
  String loginId;
  String name;
  Long ctime;
  Long mtime;
  Long dtime;


  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    OrganizationUser other = (OrganizationUser) obj;
    return Objects.equals(id, other.id);
  }
}
