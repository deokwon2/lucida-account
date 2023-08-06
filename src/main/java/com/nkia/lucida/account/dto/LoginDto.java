package com.nkia.lucida.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "회원 로그인을 위한 DTO")
public class LoginDto {

  @NotEmpty
  @Schema(description = "계정", example = "admin")
  private String loginId;

  @NotEmpty
  @Schema(description = "암호", example = "password")
  private String password;


  @NotEmpty
  @Schema(description = "조직 ID", example = "")
  private String organizationId;
}
