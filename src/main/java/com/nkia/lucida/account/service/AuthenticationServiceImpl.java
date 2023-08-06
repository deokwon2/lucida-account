package com.nkia.lucida.account.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.nkia.lucida.account.constants.AccountConstant;
import com.nkia.lucida.account.entity.Role;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.kafka.AccountKafkaProducer;
import com.nkia.lucida.account.repository.RoleRepository;
import com.nkia.lucida.account.repository.UserRepository;
import com.nkia.lucida.avro.ActionType;
import com.nkia.lucida.avro.TargetType;
import com.nkia.lucida.common.auth.AuthToken;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.auth.SecurityContext;
import com.nkia.lucida.common.exception.AuthErrorCode;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.mongodb.TenantContextHolder;


@Service
public class AuthenticationServiceImpl implements AuthenticationService {



  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtTokenService jwtTokenService;
  private final AccountKafkaProducer accountKafkaProducer;

  public AuthenticationServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
      JwtTokenService jwtTokenService, AccountKafkaProducer accountKafkaProducer) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.jwtTokenService = jwtTokenService;
    this.accountKafkaProducer = accountKafkaProducer;
  }



  @Override
  public List<Map<String, String>> login(String loginId, String password) {
    User user = userRepository.findUserByLoginIdAndDtimeIsNull(loginId);
    if (user == null) {
      throw new AuthException("Login failed.", AuthErrorCode.ACCOUNT_OR_PASSWORD_IS_INCORRECT,
          loginId);
    }

    if (!verifyPassword(user, password)) {
      throw new AuthException("Login failed.", AuthErrorCode.ACCOUNT_OR_PASSWORD_IS_INCORRECT,
          loginId);
    }

    if (user.getLocked()) {
      throw new AuthException("Login failed.", AuthErrorCode.IS_LOCKED, loginId);
    }

    List<Map<String, String>> ids = new ArrayList<>();
    TenantContextHolder.INSTANCE.setTenantId(AccountConstant.DATABASE_SHARED);
    user.getOrganizations().forEach(organization -> {
      Map<String, String> item = new HashMap<>();
      item.put("organizationId", organization.getId());
      item.put("name", organization.getName());
      ids.add(item);
    });
    TenantContextHolder.INSTANCE.clear();
    return ids;
  }



  @Override
  public AuthToken login(String loginId, String password, String organizationId) {

    User user = userRepository.findUserByLoginIdAndDtimeIsNull(loginId);

    if (user == null) {
      throw new AuthException("Login failed.", AuthErrorCode.ACCOUNT_OR_PASSWORD_IS_INCORRECT,
          loginId);
    }

    if (!verifyPassword(user, password)) {
      throw new AuthException("Login failed.", AuthErrorCode.ACCOUNT_OR_PASSWORD_IS_INCORRECT,
          loginId);
    }

    if (user.getLocked()) {
      throw new AuthException("Login failed.", AuthErrorCode.IS_LOCKED, loginId);
    }

    TenantContextHolder.INSTANCE.setTenantId(organizationId);

    List<Role> roles = roleRepository.findByUsers_idAndDtimeIsNull(user.getId());
    List<String> roleIds = roles.stream().map(i -> i.getId()).toList();

    String locale = user.getLocale() != null ? toLocaleString(user.getLocale()) : "";
    AuthToken response = jwtTokenService.createJwtToken(user.getId(), user.getLoginId(),
        organizationId, roleIds, locale);
    TenantContextHolder.INSTANCE.clear();

    user.setLastLoginTime(Instant.now().getEpochSecond());
    userRepository.save(user);

    // 로그인 성공 topic 발행
    accountKafkaProducer.send(user.getId(), organizationId, TargetType.USER, ActionType.LOGIN,
        user.getId());

    return response;
  }



  private boolean verifyPassword(User user, String password) {
    String hashedPassword = SecurityContext.INSTANCE.hash(password, user.getSalt());
    return hashedPassword.equals(user.getPassword());
  }



  private String toLocaleString(Locale locale) {
    return locale.getCountry() != null && !"".equals(locale.getCountry())
        ? locale.getLanguage() + "-" + locale.getCountry()
        : locale.getLanguage();
  }
}
