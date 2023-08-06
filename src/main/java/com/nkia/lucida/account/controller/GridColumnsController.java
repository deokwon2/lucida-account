package com.nkia.lucida.account.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nkia.lucida.account.dto.GroupInfoDto;
import com.nkia.lucida.account.dto.OrganizationInfoDto;
import com.nkia.lucida.account.dto.RoleInfoDto;
import com.nkia.lucida.account.dto.UserInfoDto;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.dto.GridColumnDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "0. GridColumns Response", description = "")
public class GridColumnsController {

  private final JwtTokenService jwtTokenService;

  public GridColumnsController(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }


  @PostMapping(path = "/account/test/gridcolumns")
  public ApiResponseData<Object> testGgetGridcolumns(@RequestParam UrlList urlList) {

    String grid = urlList.name();
    grid = grid.replaceAll("\\$", "\\/").replaceAll("_", "-");
    grid = grid.indexOf("/") > 0 ? "/" + grid : grid;

    GridColumnDataDto response = null;
    response = response != null ? response : startWith_user(grid);
    response = response != null ? response : startWith_group(grid);
    response = response != null ? response : startWith_organization(grid);
    response = response != null ? response : startWith_role(grid);

    return ApiResponseData.createSuccess(response);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
  @PostMapping(path = "/account/gridcolumns", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> getGridcolumns(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridColumnDataDto gridColumnDataDto) {

    jwtTokenService.getLoginIdFromBearerToken(headerAuthorization);

    String grid = gridColumnDataDto.getGrid().toLowerCase();
    grid = grid.indexOf("/") > 0 ? "/" + grid : grid;

    GridColumnDataDto response = null;
    response = response != null ? response : startWith_user(grid);
    response = response != null ? response : startWith_group(grid);
    response = response != null ? response : startWith_organization(grid);
    response = response != null ? response : startWith_role(grid);

    return ApiResponseData.createSuccess(response);
  }



  private GridColumnDataDto startWith_user(String grid) {

    if (!grid.startsWith("/user/")) {
      return null;
    }

    GridColumnDataDto response = null;
    switch (grid) {

      case "/user/list-page":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).excludeField("roles").excludeField("groups")
            .excludeField("locale").excludeField("dtime").build();
        break;

      case "/user/list-by-organization":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-all-own":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-included-in-the-organization":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-not-included-in-the-organization":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-page-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-all-own-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-included-in-the-organization-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-not-included-in-the-organization-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

      case "/user/list-by-organization-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(UserInfoDto.class).build();
        break;

    }
    return response;
  }


  private GridColumnDataDto startWith_group(String grid) {

    if (!grid.startsWith("/group/")) {
      return null;
    }

    GridColumnDataDto response = null;
    switch (grid) {

      case "/group/list":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(GroupInfoDto.class).build();
        break;

      case "/group/list-all-own":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(GroupInfoDto.class).build();
        break;

      case "/group/list-page-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(GroupInfoDto.class).build();
        break;

      case "/group/list-all-own-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(GroupInfoDto.class).build();
        break;
    }
    return response;
  }



  private GridColumnDataDto startWith_organization(String grid) {

    if (!grid.startsWith("/organization/")) {
      return null;
    }

    GridColumnDataDto response = null;
    switch (grid) {

      case "/organization/list":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(OrganizationInfoDto.class).build();
        break;

      case "/organization/list-page-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(OrganizationInfoDto.class).build();
        break;
    }
    return response;
  }


  private GridColumnDataDto startWith_role(String grid) {

    if (!grid.startsWith("/role/")) {
      return null;
    }

    GridColumnDataDto response = null;
    switch (grid) {

      case "/role/list":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(RoleInfoDto.class).build();
        break;

      case "/role/list-page-filter":
        response = GridColumnDataDto.builder().domain("account").grid(grid)
            .responseClazz(RoleInfoDto.class).build();
        break;
    }
    return response;
  }


  private enum UrlList {

    $user$list_page,

    $user$list_by_organization,

    $user$list_all_own,

    $user$list_included_in_the_organization,

    $user$list_not_included_in_the_organization,

    $user$list_page_filter,

    $user$list_all_own_filter,

    $user$list_included_in_the_organization_filter,

    $user$list_not_included_in_the_organization_filter,

    $user$list_by_organization_filter,

    $group$list,

    $group$list_all_own,

    $group$list_page_filter,

    $group$list_all_own_filter,

    $organization$list,

    $organization$list_page_filter,

    $role$list,

    $role$list_page_filter;
  }
}


