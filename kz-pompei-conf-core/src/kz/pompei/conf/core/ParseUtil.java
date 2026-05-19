package kz.pompei.conf.core;

import java.lang.reflect.Type;
import lombok.NonNull;

public class ParseUtil {

  public static Object parseStrToGenericType(String valueStr, @NonNull DynamicParams dynamicParams, @NonNull Type genericReturnType) {
    return valueStr;
  }
}
