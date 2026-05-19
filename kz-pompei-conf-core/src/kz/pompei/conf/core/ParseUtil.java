package kz.pompei.conf.core;

import java.lang.reflect.Type;
import lombok.NonNull;

public class ParseUtil {

  public static Object parseStrToGenericType(String valueStr, @NonNull DynamicParams dynamicParams, @NonNull Type genericReturnType) {
    if (genericReturnType == String.class) return valueStr;
    if (valueStr == null) return defaultNullValue(genericReturnType);
    if (genericReturnType == boolean.class || genericReturnType == Boolean.class) return parseBoolean(valueStr);
    if (genericReturnType == byte.class || genericReturnType == Byte.class) return Byte.parseByte(valueStr);
    if (genericReturnType == short.class || genericReturnType == Short.class) return Short.parseShort(valueStr);
    if (genericReturnType == int.class || genericReturnType == Integer.class) return Integer.parseInt(valueStr);
    if (genericReturnType == long.class || genericReturnType == Long.class) return Long.parseLong(valueStr);
    if (genericReturnType == float.class || genericReturnType == Float.class) return Float.parseFloat(valueStr);
    if (genericReturnType == double.class || genericReturnType == Double.class) return Double.parseDouble(valueStr);
    if (genericReturnType == char.class || genericReturnType == Character.class) {
      if (valueStr.length() != 1) throw new IllegalArgumentException("hVMkE7Yv2t :: Cannot parse char from string: " + valueStr);
      return valueStr.charAt(0);
    }
    return valueStr;
  }

  private static boolean parseBoolean(@NonNull String valueStr) {
    return switch (valueStr.toLowerCase()) {
      case "t", "true", "1", "on", "yes", "y", "да" -> true;
      default -> false;
    };
  }

  private static Object defaultNullValue(@NonNull Type genericReturnType) {
    if (genericReturnType == boolean.class) return false;
    if (genericReturnType == byte.class) return (byte) 0;
    if (genericReturnType == short.class) return (short) 0;
    if (genericReturnType == int.class) return 0;
    if (genericReturnType == long.class) return 0L;
    if (genericReturnType == float.class) return 0F;
    if (genericReturnType == double.class) return 0D;
    if (genericReturnType == char.class) return (char) 0;
    return null;
  }
}
