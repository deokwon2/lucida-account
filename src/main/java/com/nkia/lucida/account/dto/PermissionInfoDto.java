package com.nkia.lucida.account.dto;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import com.nkia.lucida.account.entity.Permission;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionInfoDto implements Comparable<PermissionInfoDto> {
  private long index;
  private String id;
  private Map<Locale, String> group;
  private Map<Locale, String> name;
  private Boolean own;

  public PermissionInfoDto toDto(Permission source) {
    BeanUtils.copyProperties(source, this);
    return this;
  }



  @SuppressWarnings("unused")
  private int compare(PermissionInfoDto o1, PermissionInfoDto o2) {
    if (o1.getId() == o2.getId()) {
      // Nulls or exact equality
      return 0;
    } else if (o1.getId() == null) {
      // s1 null and s2 not null, so s1 greater
      return 1;
    } else if (o2.getId() == null) {
      // s2 null and s1 not null, so s1 less
      return -1;
    } else {
      return o2.getId().compareTo(o1.getId());
    }
  }


  @Override
  public int compareTo(PermissionInfoDto o) {
    // return Comparator.comparing((PermissionInfoDto s) -> s.id).compare(this, o);
    return Comparator
        .comparing(PermissionInfoDto::getId, Comparator.nullsFirst(Comparator.naturalOrder()))
        .compare(this, o);
  }
}
