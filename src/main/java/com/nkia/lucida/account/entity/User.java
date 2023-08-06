package com.nkia.lucida.account.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import com.nkia.lucida.common.mongodb.MongoDBSharedCollection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;


@Getter
@Setter
@Document("user")
@MongoDBSharedCollection
public class User {

  @Id
  private String id;

  private String loginId;

  private String email;
  private String password;
  private String name;
  private String phone;
  private String salt;
  private Long ctime;
  private Long mtime;
  private Long dtime;
  private String defaultOrganizationId;
  private Locale locale;
  private String description;
  private String category;
  private Boolean needChangePassword;
  private Boolean locked;
  private Long lastLoginTime;

  @Singular("organizations")
  private Set<UserOrganization> organizations;

  // @Transient
  private Boolean own;

  @Builder
  public User(String id, String loginId, String email, String password, String name, String phone,
      String salt, Long ctime, Long mtime, Long dtime, String defaultOrganizationId, Locale locale,
      String description, String category, Boolean needChangePassword, Boolean locked,
      Long lastLoginTime, Set<UserOrganization> organizations, Boolean own) {

    this.id = id;
    this.loginId = loginId;
    this.email = email;
    this.password = password;
    this.name = name;
    this.phone = phone;
    this.salt = salt;
    this.ctime = ctime;
    this.mtime = mtime;
    this.dtime = dtime;
    this.defaultOrganizationId = defaultOrganizationId;
    this.locale = locale;
    this.description = description;
    this.category = category;
    this.needChangePassword = needChangePassword;
    this.locked = locked;
    this.lastLoginTime = lastLoginTime;
    this.organizations = organizations;
    this.own = own;

  }



  @Transient
  public void addOrganization(Organization organization) {
    if (organization == null) {
      return;
    }

    long time = Instant.now().getEpochSecond();
    UserOrganization userOrganization = UserOrganization.builder().id(organization.getId())
        .name(organization.getName()).ctime(time).mtime(time).build();
    if (this.organizations == null) {
      this.organizations = new HashSet<>();
    }
    this.organizations.add(userOrganization);
  }


  @Transient
  public void addOrganization(Collection<Organization> organizations) {
    if (organizations == null) {
      return;
    }

    for (Organization organization : organizations) {
      addOrganization(organization);
    }
  }


  @Transient
  public void excludeOrganization(Organization organization) {
    if (organization == null) {
      return;
    }

    if (this.organizations == null) {
      return;
    }
    this.organizations.removeIf(o -> o.getId().equals(organization.getId()));
  }


  @Transient
  public void excludeOrganization(Collection<Organization> organizations) {
    if (organizations == null) {
      return;
    }

    for (Organization organization : organizations) {
      this.excludeOrganization(organization);
    }
  }


  @Transient
  public boolean hasOrganization(String organizationId) {
    return Optional.ofNullable(this.organizations).orElseGet(Collections::emptySet).stream()
        .anyMatch(o -> o.getId().equals(organizationId));
  }
}
