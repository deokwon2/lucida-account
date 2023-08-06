package com.nkia.lucida.account.entity;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import com.nkia.lucida.common.mongodb.MongoDBSharedCollection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Document(collection = "permission")
@MongoDBSharedCollection
public class Permission {

  private String id;
  private Map<Locale, String> group;
  private Map<Locale, String> name;

  @Transient
  private Boolean own;


  @Builder
  public Permission(String id, Map<Locale, String> group, Map<Locale, String> name) {
    super();
    this.id = id;
    this.group = group;
    this.name = name;
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
    Permission other = (Permission) obj;
    return Objects.equals(id, other.id);
  }
}
