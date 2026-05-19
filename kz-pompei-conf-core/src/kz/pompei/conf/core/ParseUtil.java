package kz.pompei.conf.core;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import lombok.NonNull;

public class ParseUtil {

  public static Object parseStrToGenericType(String valueStr, @NonNull DynamicParams dynamicParams, @NonNull Type genericReturnType) {
    if (valueStr == null) return defaultNullValue(genericReturnType);
    valueStr = resolveDynamicParams(valueStr, dynamicParams);
    valueStr = resolveStandardSubstitutions(valueStr);
    if (genericReturnType == String.class) return valueStr;
    if (genericReturnType == boolean.class || genericReturnType == Boolean.class) return parseBoolean(valueStr);
    if (genericReturnType == byte.class || genericReturnType == Byte.class) return parseByte(valueStr);
    if (genericReturnType == short.class || genericReturnType == Short.class) return parseShort(valueStr);
    if (genericReturnType == int.class || genericReturnType == Integer.class) return parseInt(valueStr);
    if (genericReturnType == long.class || genericReturnType == Long.class) return parseLong(valueStr);
    if (genericReturnType == float.class || genericReturnType == Float.class) return Float.parseFloat(normalizeNumber(valueStr));
    if (genericReturnType == double.class || genericReturnType == Double.class) return Double.parseDouble(normalizeNumber(valueStr));
    if (genericReturnType == BigDecimal.class) return parseBigDecimal(valueStr);
    if (genericReturnType == BigInteger.class) return parseBigInteger(valueStr);
    if (genericReturnType == char.class || genericReturnType == Character.class) {
      if (valueStr.length() != 1) throw new IllegalArgumentException("hVMkE7Yv2t :: Cannot parse char from string: " + valueStr);
      return valueStr.charAt(0);
    }
    return valueStr;
  }

  private static String resolveStandardSubstitutions(@NonNull String valueStr) {
    StringBuilder resolved = new StringBuilder(valueStr.length());
    for (int i = 0; i < valueStr.length(); i++) {
      char ch = valueStr.charAt(i);
      if (ch != '\\' || i == valueStr.length() - 1) {
        resolved.append(ch);
        continue;
      }

      char next = valueStr.charAt(++i);
      switch (next) {
        case 'n' -> resolved.append('\n');
        case 't' -> resolved.append('\t');
        case 'r' -> resolved.append('\r');
        case 'b' -> resolved.append('\b');
        case 'f' -> resolved.append('\f');
        case '\\' -> resolved.append('\\');
        case '"' -> resolved.append('"');
        case '\'' -> resolved.append('\'');
        default -> resolved.append('\\').append(next);
      }
    }
    return resolved.toString();
  }

  private static String resolveDynamicParams(@NonNull String valueStr, @NonNull DynamicParams dynamicParams) {
    StringBuilder resolved = new StringBuilder(valueStr.length());
    int           index    = 0;
    while (index < valueStr.length()) {
      int start = valueStr.indexOf("$ENV{", index);
      if (start < 0) {
        resolved.append(valueStr, index, valueStr.length());
        break;
      }

      int end = valueStr.indexOf('}', start + 5);
      if (end < 0) {
        resolved.append(valueStr, index, valueStr.length());
        break;
      }

      resolved.append(valueStr, index, start);
      String envName  = valueStr.substring(start + 5, end);
      String envValue = dynamicParams.env(envName);
      if (envValue != null) resolved.append(envValue);
      index = end + 1;
    }
    return resolved.toString();
  }

  private static byte parseByte(@NonNull String valueStr) {
    return parseInteger(valueStr).byteValueExact();
  }

  private static short parseShort(@NonNull String valueStr) {
    return parseInteger(valueStr).shortValueExact();
  }

  private static int parseInt(@NonNull String valueStr) {
    return parseInteger(valueStr).intValueExact();
  }

  private static long parseLong(@NonNull String valueStr) {
    return parseInteger(valueStr).longValueExact();
  }

  private static BigDecimal parseBigDecimal(@NonNull String valueStr) {
    String normalized = normalizeNumber(valueStr);
    if (normalized.isEmpty()) return BigDecimal.ZERO;
    return new BigDecimal(normalized);
  }

  private static BigInteger parseBigInteger(@NonNull String valueStr) {
    return parseInteger(valueStr).toBigIntegerExact();
  }

  private static BigDecimal parseInteger(@NonNull String valueStr) {
    return new BigDecimal(normalizeNumber(valueStr)).setScale(0, RoundingMode.HALF_UP);
  }

  private static String normalizeNumber(@NonNull String valueStr) {
    StringBuilder normalized = new StringBuilder(valueStr.length());
    for (int i = 0; i < valueStr.length(); i++) {
      char ch = valueStr.charAt(i);
      if (Character.isWhitespace(ch) || ch == '_' || ch == '\\' || ch == '\b' || ch == '\f') continue;
      normalized.append(ch == ',' ? '.' : ch);
    }
    return normalized.toString();
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
    if (genericReturnType == BigDecimal.class) return BigDecimal.ZERO;
    if (genericReturnType == BigInteger.class) return BigInteger.ZERO;
    return null;
  }
}
