package com.nkia.lucida.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "비밀번호 변경을 위한 DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordDto {

  @NotEmpty
  @Schema(description = "id", example = "6433b8019af0ce65d914bb4b")
  private String id;

  @NotEmpty
  @Schema(description = "계정 로그인 id", example = "admin")
  private String loginId;

  @Schema(description = "현재 비밀번호", example = "password")
  private String currentPassword;

  @NotEmpty
  @Schema(description = "새로운 비밀번호", example = "newPassword")
  private String newPassword;
}
