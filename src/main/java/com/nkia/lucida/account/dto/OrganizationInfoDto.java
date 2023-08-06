package com.nkia.lucida.account.dto;

import java.util.Collection;
import java.util.TreeSet;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nkia.lucida.account.entity.Organization;
import com.nkia.lucida.account.entity.User;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "조직의 정보를 모두 포함하는 DTO")
public class OrganizationInfoDto {

  private long index;

  @Schema(description = "조직의 object Id", example = "62d4b5db118be72050057a15")
  private String id;

  @Schema(description = "조직 이름", example = "Nkia")
  private String name;

  @Schema(description = "조직 설명", example = "연구4팀")
  private String description;

  @Schema(description = "최근 수정된 시간", example = "1658107419076")
  private Long mtime;

  @Schema(description = "삭제된 시간", example = "1658107419076")
  private Long dtime;

  @Schema(description = "생성된 시간", example = "1658107419076")
  private Long ctime;

  @ArraySchema(arraySchema = @Schema(description = "조직에 포함된 사용자 목록"))
  private TreeSet<UserInfoDto> users = new TreeSet<>();


  private int userAmount = 0;

  public OrganizationInfoDto() {}

  public OrganizationInfoDto(Organization source) {
    BeanUtils.copyProperties(source, this);

    if (source.getUsers() != null) {
      this.setUserAmount(source.getUsers().size());
    }
  }

  public OrganizationInfoDto(Organization source, Collection<User> users) {
    BeanUtils.copyProperties(source, this);

    if (source.getUsers() != null) {
      this.setUserAmount(source.getUsers().size());
    }

    if (users != null) {
      for (User user : users) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.toDto(user);
        this.users.add(userInfoDto);
      }
    }
  }
}
