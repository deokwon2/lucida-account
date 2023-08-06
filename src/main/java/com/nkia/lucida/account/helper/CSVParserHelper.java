package com.nkia.lucida.account.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.validation.constraints.NotBlank;

public enum CSVParserHelper {

  INSTANCE;


  public <T> List<T> readCSVFile(Class<T> clazz, InputStream inputStream) throws IOException {
    try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      List<T> beans = new CsvToBeanBuilder<T>(reader).withType(clazz)
          .withIgnoreLeadingWhiteSpace(true).withSeparator(',').build().parse();
      List<T> result = new ArrayList<>();
      for (T bean : beans) {
        if (isValidBean(bean)) {
          result.add(bean);
        }
      }
      return result;
    }
  }

  private static <T> boolean isValidBean(T bean) {
    // Check that all required fields are not blank
    // If a required field is blank, skip the line and do not add to the result list
    // Here, we assume that required fields are marked with the @NotBlank annotation
    for (Field field : bean.getClass().getDeclaredFields()) {
      try {
        String fieldName = field.getName();
        String getterName =
            "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String setterName =
            "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Object value = bean.getClass().getMethod(getterName).invoke(bean);
        if (value == null || (value instanceof String string && (string).isEmpty())) {
          if (field.isAnnotationPresent(NotBlank.class)) {
            return false;
          } else {
            bean.getClass().getMethod(setterName, field.getType()).invoke(bean, (Object) null);
          }
        }
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        // ignore, move on to the next field
      }
    }
    return true;
  }
}
