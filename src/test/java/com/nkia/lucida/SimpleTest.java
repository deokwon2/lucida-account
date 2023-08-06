package com.nkia.lucida;

import java.util.Iterator;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import com.nkia.lucida.account.dto.UserInfoDto;

public class SimpleTest {

  @Test
  public void myTest() {


    UserInfoDto u1 = new UserInfoDto();
    u1.setLoginId("A");
    u1.setName("A");


    UserInfoDto u2 = new UserInfoDto();
    u2.setLoginId("B");
    u2.setName(null);

    TreeSet<UserInfoDto> userInfoDtos = new TreeSet<>();
    userInfoDtos.add(u2);
    userInfoDtos.add(u1);


    Iterator<UserInfoDto> value = userInfoDtos.iterator();
    while (value.hasNext()) {
      System.out.println(value.next().toString());
    }

  }
}
