package com.nkia.lucida.account.service;

import java.util.List;
import java.util.Map;
import com.nkia.lucida.common.auth.AuthToken;

public interface AuthenticationService {

  public List<Map<String, String>> login(String loginId, String password);

  public AuthToken login(String loginId, String password, String organizationId);
}
