package kz.pompei.conf.core;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class ParseUtil {
  private static final MathContext FLOATING_POINT_MATH_CONTEXT = new MathContext(50, RoundingMode.HALF_UP);

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
    if (genericReturnType == float.class || genericReturnType == Float.class) return parseBigDecimal(valueStr).floatValue();
    if (genericReturnType == double.class || genericReturnType == Double.class) return parseBigDecimal(valueStr).doubleValue();
    if (genericReturnType == BigDecimal.class) return parseBigDecimal(valueStr);
    if (genericReturnType == BigInteger.class) return parseBigInteger(valueStr);
    if (genericReturnType == char.class || genericReturnType == Character.class) {
      if (valueStr.length() != 1) throw new IllegalArgumentException("hVMkE7Yv2t :: Cannot parse char from string: " + valueStr);
      return valueStr.charAt(0);
    }
    return valueStr;
  }

  private static @NonNull String resolveStandardSubstitutions(@NonNull String valueStr) {
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
    return new DecimalExpressionParser(normalized).parse();
  }

  private static BigInteger parseBigInteger(@NonNull String valueStr) {
    return parseInteger(valueStr).toBigIntegerExact();
  }

  private static BigDecimal parseInteger(@NonNull String valueStr) {
    String normalized = normalizeNumber(valueStr);
    if (normalized.indexOf('.') >= 0) return new DecimalExpressionParser(normalized).parse().setScale(0, RoundingMode.HALF_UP);
    return new BigDecimal(new IntegerExpressionParser(normalized).parse());
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
    Boolean parsed = switch (valueStr.toLowerCase()) {
      case "t", "true", "1", "on", "yes", "y", "да" -> true;
      case "f", "false", "0", "off", "no", "n", "" -> false;
      default -> null;
    };
    if (parsed != null) return parsed;

    String normalized = normalizeNumber(valueStr);
    if (normalized.isEmpty()) return false;
    try {
      if (normalized.indexOf('.') >= 0) return new DecimalExpressionParser(normalized).parse().abs().compareTo(new BigDecimal("0.001")) >= 0;
      return new IntegerExpressionParser(normalized).parse().compareTo(BigInteger.ZERO) != 0;
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  private static final class IntegerExpressionParser {
    private final String value;
    private       int    index;

    private IntegerExpressionParser(@NonNull String value) {
      this.value = value;
    }

    private BigInteger parse() {
      BigInteger result = parseAddSubtract();
      if (index != value.length()) throw new IllegalArgumentException("Ht2RaK7p9m :: Cannot parse integer expression: " + value);
      return result;
    }

    private BigInteger parseAddSubtract() {
      BigInteger result = parseMultiplyDivide();
      while (index < value.length()) {
        char operator = value.charAt(index);
        if (operator != '+' && operator != '-') break;
        index++;
        BigInteger right = parseMultiplyDivide();
        result = operator == '+' ? result.add(right) : result.subtract(right);
      }
      return result;
    }

    private BigInteger parseMultiplyDivide() {
      BigInteger result = parseUnary();
      while (index < value.length()) {
        char operator = value.charAt(index);
        if (operator != '*' && operator != '/') break;
        index++;
        BigInteger right = parseUnary();
        result = operator == '*' ? result.multiply(right) : result.divide(right);
      }
      return result;
    }

    private BigInteger parseUnary() {
      if (index < value.length() && value.charAt(index) == '+') {
        index++;
        return parseUnary();
      }
      if (index < value.length() && value.charAt(index) == '-') {
        index++;
        return parseUnary().negate();
      }
      return parseAtom();
    }

    private BigInteger parseAtom() {
      if (index < value.length() && value.charAt(index) == '(') {
        index++;
        BigInteger result = parseAddSubtract();
        if (index >= value.length() || value.charAt(index) != ')') {
          throw new IllegalArgumentException("Pq4Nf8Ly2s :: Missing closing parenthesis in integer expression: " + value);
        }
        index++;
        return result;
      }

      int start = index;
      while (index < value.length() && Character.isDigit(value.charAt(index))) index++;
      if (start == index) throw new IllegalArgumentException("Wm6Ze3Tq1c :: Expected integer number in expression: " + value);
      return new BigInteger(value.substring(start, index));
    }
  }

  private static final class DecimalExpressionParser {
    private final String value;
    private       int    index;

    private DecimalExpressionParser(@NonNull String value) {
      this.value = value;
    }

    private BigDecimal parse() {
      BigDecimal result = parseAddSubtract();
      if (index != value.length()) throw new IllegalArgumentException("Fz8Kv5Jh3n :: Cannot parse decimal expression: " + value);
      return result;
    }

    private BigDecimal parseAddSubtract() {
      BigDecimal result = parseMultiplyDivide();
      while (index < value.length()) {
        char operator = value.charAt(index);
        if (operator != '+' && operator != '-') break;
        index++;
        BigDecimal right = parseMultiplyDivide();
        result = operator == '+' ? result.add(right, FLOATING_POINT_MATH_CONTEXT) : result.subtract(right, FLOATING_POINT_MATH_CONTEXT);
      }
      return result;
    }

    private BigDecimal parseMultiplyDivide() {
      BigDecimal result = parseUnary();
      while (index < value.length()) {
        char operator = value.charAt(index);
        if (operator != '*' && operator != '/') break;
        index++;
        BigDecimal right = parseUnary();
        result = operator == '*' ? result.multiply(right, FLOATING_POINT_MATH_CONTEXT) : result.divide(right, FLOATING_POINT_MATH_CONTEXT);
      }
      return result;
    }

    private BigDecimal parseUnary() {
      if (index < value.length() && value.charAt(index) == '+') {
        index++;
        return parseUnary();
      }
      if (index < value.length() && value.charAt(index) == '-') {
        index++;
        return parseUnary().negate(FLOATING_POINT_MATH_CONTEXT);
      }
      return parseAtom();
    }

    private BigDecimal parseAtom() {
      if (index < value.length() && value.charAt(index) == '(') {
        index++;
        BigDecimal result = parseAddSubtract();
        if (index >= value.length() || value.charAt(index) != ')') {
          throw new IllegalArgumentException("Ry9Md4Pc6v :: Missing closing parenthesis in decimal expression: " + value);
        }
        index++;
        return result;
      }

      int     start  = index;
      boolean hasDot = false;
      while (index < value.length()) {
        char ch = value.charAt(index);
        if (Character.isDigit(ch)) {
          index++;
          continue;
        }
        if (ch == '.' && !hasDot) {
          hasDot = true;
          index++;
          continue;
        }
        break;
      }
      if (start == index) throw new IllegalArgumentException("Gc7Vu2Bp5x :: Expected decimal number in expression: " + value);
      return new BigDecimal(value.substring(start, index), FLOATING_POINT_MATH_CONTEXT);
    }
  }

  private static @Nullable Object defaultNullValue(@NonNull Type genericReturnType) {
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
