package kz.pompei.hotconfig.core;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class ParseUtil {
  private static final MathContext FLOATING_POINT_MATH_CONTEXT = new MathContext(50, RoundingMode.HALF_UP);

  public static @NonNull String unescape(@NonNull String value) {
    StringBuilder result = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\\' && i + 1 < value.length()) {
        char next = value.charAt(++i);
        if (next == 'n') {
          result.append('\n');
        } else {
          result.append(next);
        }
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public static @Nullable Object parseStrToGenericType(@Nullable String valueStr,
                                                       @NonNull EnvSrc envSrc,
                                                       @NonNull Type genericReturnType) {

    if (valueStr == null) return defaultNullValue(genericReturnType);
    valueStr = resolveEnvSrc(valueStr, envSrc);
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

  private static @NonNull String resolveEnvSrc(@NonNull String valueStr, @NonNull EnvSrc envSrc) {
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
      String           envName  = valueStr.substring(start + 5, end);
      @Nullable String envValue = envSrc.env(envName);
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

  private static @NonNull BigDecimal parseBigDecimal(@NonNull String valueStr) {
    String normalized = normalizeNumber(valueStr);
    if (normalized.isEmpty()) return BigDecimal.ZERO;
    return new DecimalExpressionParser(normalized).parse();
  }

  private static @NonNull BigInteger parseBigInteger(@NonNull String valueStr) {
    return parseInteger(valueStr).toBigIntegerExact();
  }

  private static @NonNull BigDecimal parseInteger(@NonNull String valueStr) {
    String normalized = normalizeNumber(valueStr);
    if (normalized.indexOf('.') >= 0) return new DecimalExpressionParser(normalized).parse().setScale(0, RoundingMode.HALF_UP);
    try {
      return new BigDecimal(new IntegerExpressionParser(normalized).parse());
    } catch (RuntimeException e) {
      if (hasDecimalExponent(normalized)) return new DecimalExpressionParser(normalized).parse().setScale(0, RoundingMode.HALF_UP);
      throw e;
    }
  }

  private static @NonNull String normalizeNumber(@NonNull String valueStr) {
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
      try {
        return new IntegerExpressionParser(normalized).parse().compareTo(BigInteger.ZERO) != 0;
      } catch (RuntimeException e) {
        if (hasDecimalExponent(normalized)) return new DecimalExpressionParser(normalized).parse().abs().compareTo(new BigDecimal("0.001")) >= 0;
        throw e;
      }
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  private static boolean hasDecimalExponent(@NonNull String valueStr) {
    return valueStr.indexOf('.') >= 0 || valueStr.indexOf('e') >= 0 || valueStr.indexOf('E') >= 0;
  }

  private static boolean startsWithBasePrefix(@NonNull String value, int index) {
    if (index + 1 >= value.length() || value.charAt(index) != '0') return false;
    char prefix = value.charAt(index + 1);
    return prefix == 'x' || prefix == 'X' || prefix == 'b' || prefix == 'B' || prefix == 'o' || prefix == 'O';
  }

  private static @NonNull IntegerLiteral parseIntegerLiteral(@NonNull String value, int start) {
    int radix       = 10;
    int digitsStart = start;
    if (startsWithBasePrefix(value, start)) {
      char prefix = value.charAt(start + 1);
      radix       = switch (prefix) {
        case 'x', 'X' -> 16;
        case 'b', 'B' -> 2;
        case 'o', 'O' -> 8;
        default -> throw new IllegalStateException("Dk4Nm8Vs2q :: Unsupported integer literal prefix: " + prefix);
      };
      digitsStart = start + 2;
    }

    int index = digitsStart;
    while (index < value.length() && Character.digit(value.charAt(index), radix) >= 0) index++;
    if (index == digitsStart) throw new IllegalArgumentException("Wm6Ze3Tq1c :: Expected integer number in expression: " + value);
    return new IntegerLiteral(new BigInteger(value.substring(digitsStart, index), radix), index);
  }

  private record IntegerLiteral(@NonNull BigInteger value, int end) {}

  private static final class IntegerExpressionParser {
    private final @NonNull String value;
    private                int    index;

    private IntegerExpressionParser(@NonNull String value) {
      this.value = value;
    }

    private @NonNull BigInteger parse() {
      BigInteger result = parseAddSubtract();
      if (index != value.length()) throw new IllegalArgumentException("Ht2RaK7p9m :: Cannot parse integer expression: " + value);
      return result;
    }

    private @NonNull BigInteger parseAddSubtract() {
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

    private @NonNull BigInteger parseMultiplyDivide() {
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

    private @NonNull BigInteger parseUnary() {
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

    private @NonNull BigInteger parseAtom() {
      if (index < value.length() && value.charAt(index) == '(') {
        index++;
        BigInteger result = parseAddSubtract();
        if (index >= value.length() || value.charAt(index) != ')') {
          throw new IllegalArgumentException("Pq4Nf8Ly2s :: Missing closing parenthesis in integer expression: " + value);
        }
        index++;
        return result;
      }

      IntegerLiteral literal = parseIntegerLiteral(value, index);
      index = literal.end;
      return literal.value;
    }
  }

  private static final class DecimalExpressionParser {
    private final @NonNull String value;
    private                int    index;

    private DecimalExpressionParser(@NonNull String value) {
      this.value = value;
    }

    private @NonNull BigDecimal parse() {
      BigDecimal result = parseAddSubtract();
      if (index != value.length()) throw new IllegalArgumentException("Fz8Kv5Jh3n :: Cannot parse decimal expression: " + value);
      return result;
    }

    private @NonNull BigDecimal parseAddSubtract() {
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

    private @NonNull BigDecimal parseMultiplyDivide() {
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

    private @NonNull BigDecimal parseUnary() {
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

    private @NonNull BigDecimal parseAtom() {
      if (index < value.length() && value.charAt(index) == '(') {
        index++;
        BigDecimal result = parseAddSubtract();
        if (index >= value.length() || value.charAt(index) != ')') {
          throw new IllegalArgumentException("Ry9Md4Pc6v :: Missing closing parenthesis in decimal expression: " + value);
        }
        index++;
        return result;
      }

      if (startsWithBasePrefix(value, index)) {
        IntegerLiteral literal = parseIntegerLiteral(value, index);
        index = literal.end;
        return new BigDecimal(literal.value, FLOATING_POINT_MATH_CONTEXT);
      }

      int     start       = index;
      boolean hasDot      = false;
      boolean hasExponent = false;
      while (index < value.length()) {
        char ch = value.charAt(index);
        if (Character.isDigit(ch)) {
          index++;
          continue;
        }
        if (ch == '.' && !hasDot && !hasExponent) {
          hasDot = true;
          index++;
          continue;
        }
        if ((ch == 'e' || ch == 'E') && !hasExponent && index > start) {
          hasExponent = true;
          index++;
          if (index < value.length() && (value.charAt(index) == '+' || value.charAt(index) == '-')) index++;
          continue;
        }
        break;
      }
      if (start == index) throw new IllegalArgumentException("Gc7Vu2Bp5x :: Expected decimal number in expression: " + value);
      return new BigDecimal(value.substring(start, index), FLOATING_POINT_MATH_CONTEXT);
    }
  }

  public static @Nullable Object defaultNullValue(@NonNull Type genericReturnType) {
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
