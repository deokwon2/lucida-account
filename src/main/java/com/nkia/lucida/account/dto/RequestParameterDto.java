package com.nkia.lucida.account.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestParameterDto<T> {
  private T parameter;
}
