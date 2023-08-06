package com.nkia.lucida.account.entity;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupUser {

  private String id;
  private String loginId;
  private String name;

  private Long ctime;
  private Long mtime;
  private Long dtime;

  @Builder
  public GroupUser(String id, String loginId, String name, Long ctime, Long mtime, Long dtime) {
    super();
    this.id = id;
    this.loginId = loginId;
    this.name = name;
    this.ctime = ctime;
    this.mtime = mtime;
    this.dtime = dtime;
  }

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
    GroupUser other = (GroupUser) obj;
    return Objects.equals(id, other.id);
  }
}
