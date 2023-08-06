package com.nkia.lucida.account.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nkia.lucida.account.dto.OrganizationInfoDto;
import com.nkia.lucida.account.dto.OrganizationSaveDto;
import com.nkia.lucida.account.dto.RequestParameterDto;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.account.entity.User;
import com.nkia.lucida.account.service.OrganizationManagementService;
import com.nkia.lucida.account.service.UserManagementService;
import com.nkia.lucida.common.auth.JwtTokenService;
import com.nkia.lucida.common.dto.ApiResponseData;
import com.nkia.lucida.common.exception.AuthException;
import com.nkia.lucida.common.exception.GlobalErrorCode;
import com.nkia.lucida.common.mongodb.CriteriaMakeHelper;
import com.nkia.lucida.common.mongodb.GridFiltersPageableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/account")
@Tag(name = "3. Organization", description = "")
public class OrganizationManagementController {


  private final JwtTokenService jwtTokenService;
  private final OrganizationManagementService organizationManagementService;
  private final UserManagementService userManagementService;

  public OrganizationManagementController(JwtTokenService jwtTokenService,
      OrganizationManagementService organizationManagementService,
      UserManagementService userManagementService) {
    this.jwtTokenService = jwtTokenService;
    this.organizationManagementService = organizationManagementService;
    this.userManagementService = userManagementService;
  }


  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "조직을 생성한다. (admin 계정은 자동으로 포함되어 생성)")
  @PostMapping(path = "/organization/insert", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> insertOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody OrganizationSaveDto organizationSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    boolean check = userManagementService.checkDuplicateLoginId(lUserId, lOrganizationId,
        organizationSaveDto.getAddAdminLoginId());

    if (check) {
      throw new AuthException(
          "Duplicate loginId. (" + organizationSaveDto.getAddAdminLoginId() + ")",
          GlobalErrorCode.INVALID_ARGUMENTS, null);
    }

    Organization newOrganization = organizationManagementService.insertOrganization(lUserId,
        lOrganizationId, organizationSaveDto.toEntity(), organizationSaveDto.getUserIds(),
        organizationSaveDto.getAddAdminLoginId(), organizationSaveDto.getAddAdminName(),
        organizationSaveDto.getAddAdminPassword());

    return ApiResponseData.createSuccess(new OrganizationInfoDto(newOrganization));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "조직의 정보를 변경한다.")
  @PostMapping(path = "/organization/update", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> updateOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody OrganizationSaveDto organizationSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Organization updateOrganization = organizationManagementService.updateOrganization(lUserId,
        lOrganizationId, organizationSaveDto.toEntity(), organizationSaveDto.getUserIds());

    return ApiResponseData.createSuccess(new OrganizationInfoDto(updateOrganization));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "조직에서 사용자를 제거한다.",
      description = "id = 조직ID, userIds = ['사용자ID 1', '사용자ID 2' ...]")
  @PostMapping(path = "/organization/update-exclude-users",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> excludeUserFromOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody OrganizationSaveDto organizationSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Organization updateOrganization = organizationManagementService.excludeUserFromOrganization(
        lUserId, lOrganizationId, organizationSaveDto.getId(), organizationSaveDto.getUserIds());

    return ApiResponseData.createSuccess(new OrganizationInfoDto(updateOrganization));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "조직에서 사용자를 추가한다.",
      description = "id = 조직ID, userIds = ['사용자ID 1', '사용자ID 2' ...]")
  @PostMapping(path = "/organization/update-add-users", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> addUserFromOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody OrganizationSaveDto organizationSaveDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Organization updateOrganization = organizationManagementService.addUserFromOrganization(lUserId,
        lOrganizationId, organizationSaveDto.getId(), organizationSaveDto.getUserIds());

    return ApiResponseData.createSuccess(new OrganizationInfoDto(updateOrganization));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "조직을 삭제한다.",
      description = "parameter = 조직ID")
  @PostMapping(path = "/organization/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<List<String>> deleteOrganization(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<List<String>> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    organizationManagementService.deleteOrganization(lUserId, lOrganizationId, false,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(requestParameterDto.getParameter());
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "조직의 목록을 조회한다.")
  @PostMapping(path = "/organization/list")
  public ApiResponseData<Object> selectOrganizationByConditions(@Parameter(
      hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String headerAuthorization) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    List<Organization> organizations = organizationManagementService
        .selectOrganizationByConditions(lUserId, lOrganizationId, null);

    List<OrganizationInfoDto> organizationInfoDtos = organizations.stream().map(o -> {
      return new OrganizationInfoDto(o);
    }).toList();

    return ApiResponseData.createSuccess(organizationInfoDtos);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "선택한 조직의 상세 정보 및 사용자 목록을 조회 한다. (등록된 사용자는 own=true)",
      description = "parameter = 조직ID")
  @PostMapping(path = "/organization/detail", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> selectUserById(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Organization organization = organizationManagementService.selectOrganizationById(lUserId,
        lOrganizationId, requestParameterDto.getParameter());

    List<User> users = userManagementService.selectUserByOrganizationWithOwn(lUserId,
        lOrganizationId, requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(new OrganizationInfoDto(organization, users));
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")},
      summary = "동일한 조직의 이름이 존재하는 지 확인. (존재=true)", description = "parameter = 조직ID")
  @PostMapping(path = "/organization/check-duplicate-name",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseData<Object> checkDuplicateName(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody RequestParameterDto<String> requestParameterDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);

    Boolean duplicate = organizationManagementService.checkDuplicateName(lUserId, lOrganizationId,
        requestParameterDto.getParameter());

    return ApiResponseData.createSuccess(duplicate);
  }



  @Operation(security = {@SecurityRequirement(name = "bearerAuth")}, summary = "조직의 목록을 조회한다.")
  @PostMapping(path = "/organization/list-page-filter")
  public ApiResponseData<Object> selectOrganizationByConditions(
      @Parameter(hidden = true) @RequestHeader(
          value = HttpHeaders.AUTHORIZATION) String headerAuthorization,
      @RequestBody GridFiltersPageableDto gridFiltersPageableDto) {

    String lUserId = jwtTokenService.getUserIdFromBearerToken(headerAuthorization);
    String lOrganizationId = jwtTokenService.getOrganizationIdFromBearerToken(headerAuthorization);


    List<Criteria> criterias = gridFiltersPageableDto.getGridFilters().stream().map(i -> {
      return CriteriaMakeHelper.INSTANCE.get(i.getField(), i.getOperator(), i.getValues());
    }).toList();

    Page<Organization> items = organizationManagementService.selectOrganizationByConditions(lUserId,
        lOrganizationId, criterias, gridFiltersPageableDto.toPageable());


    long rowNumIndex = items.getPageable().getOffset();
    AtomicInteger index = new AtomicInteger((int) rowNumIndex);
    Page<OrganizationInfoDto> dtoPage = items.map(u -> {
      OrganizationInfoDto dto = new OrganizationInfoDto(u);
      dto.setIndex(index.incrementAndGet());
      return dto;
    });

    return ApiResponseData.createSuccess(dtoPage);
  }
}
