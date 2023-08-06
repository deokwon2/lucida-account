package com.nkia.lucida.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.nkia.lucida.account.constants.AccountConstant;
import com.nkia.lucida.common.mongodb.TenantMongoDBConfigure;

/**
 * @author henoh@nkia.co.kr on 2023-01-16
 */
@Configuration
@Import({TenantMongoDBConfigure.class})
public class TMongoDBConfigurer {


  @Value("${com.nkia.lucida.common.mongodb.global-database:shared}")
  private void setGlobalDatabase(String global_database) {
    AccountConstant.DATABASE_SHARED = global_database;
  }
}
