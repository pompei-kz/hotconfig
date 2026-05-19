package kz.pompei.conf.core;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParseUtilTest {

  @DataProvider
  public Object[][] primitiveAndBoxedTypes() {
    return new Object[][]{
      {"7", byte.class, (byte) 7},
      {"8", Byte.class, (byte) 8},
      {"300", short.class, (short) 300},
      {"301", Short.class, (short) 301},
      {"123456", int.class, 123456},
      {"654321", Integer.class, 654321},
      {"1234567890123", long.class, 1234567890123L},
      {"3210987654321", Long.class, 3210987654321L},
      {"3.25", float.class, 3.25F},
      {"4.5", Float.class, 4.5F},
      {"6.75", double.class, 6.75D},
      {"7.125", Double.class, 7.125D},
      {"12345678901234567890.125", BigDecimal.class, new BigDecimal("12345678901234567890.125")},
      {"12345678901234567890", BigInteger.class, new BigInteger("12345678901234567890")},
      {"x", char.class, 'x'},
      {"y", Character.class, 'y'},
    };
  }

  @DataProvider
  public Object[][] formattedNumericValues() {
    return new Object[][]{
      {"1 2", byte.class, (byte) 12},
      {"1_3", Byte.class, (byte) 13},
      {"3 0 0", short.class, (short) 300},
      {"3_0_1", Short.class, (short) 301},
      {"123 456", int.class, 123456},
      {"654_321", Integer.class, 654321},
      {"1 234 567 890 123", long.class, 1234567890123L},
      {"3_210_987_654_321", Long.class, 3210987654321L},
      {"3,25", float.class, 3.25F},
      {"4,5", Float.class, 4.5F},
      {"6 000,75", double.class, 6000.75D},
      {"7_000,125", Double.class, 7000.125D},
      {"12 345_678,901", BigDecimal.class, new BigDecimal("12345678.901")},
      {"12 345_678", BigInteger.class, new BigInteger("12345678")},
    };
  }

  @DataProvider
  public Object[][] numericValuesWithStandardSubstitutions() {
    return new Object[][]{
      {"1\\n2", byte.class, (byte) 12},
      {"1\\t3", Byte.class, (byte) 13},
      {"3\\r0\\b0", short.class, (short) 300},
      {"3\\f0_1", Short.class, (short) 301},
      {"123\\n456", int.class, 123456},
      {"123\\\\456", Integer.class, 123456},
      {"1\\t234\\n567\\r890\\b123", long.class, 1234567890123L},
      {"3\\f210\\\\987_654_321", Long.class, 3210987654321L},
      {"3\\t,25", float.class, 3.25F},
      {"4\\\\,5", Float.class, 4.5F},
      {"6\\n000,75", double.class, 6000.75D},
      {"7\\r000\\b,125", Double.class, 7000.125D},
      {"12\\n345_678,901", BigDecimal.class, new BigDecimal("12345678.901")},
      {"12\\t345\\r678.50", BigInteger.class, new BigInteger("12345679")},
    };
  }

  @DataProvider
  public Object[][] integerValuesWithDecimalParts() {
    return new Object[][]{
      {"7.49", byte.class, (byte) 7},
      {"7.50", Byte.class, (byte) 8},
      {"299,49", short.class, (short) 299},
      {"299,50", Short.class, (short) 300},
      {"123 456.49", int.class, 123456},
      {"123_456.50", Integer.class, 123457},
      {"1 234 567 890 123,49", long.class, 1234567890123L},
      {"1_234_567_890_123,50", Long.class, 1234567890124L},
      {"12 345 678,49", BigInteger.class, new BigInteger("12345678")},
      {"12_345_678.50", BigInteger.class, new BigInteger("12345679")},
    };
  }

  @DataProvider
  public Object[][] booleanTrueValues() {
    return new Object[][]{
      {"t"},
      {"T"},
      {"true"},
      {"TRUE"},
      {"TrUe"},
      {"1"},
      {"on"},
      {"ON"},
      {"On"},
      {"yes"},
      {"YES"},
      {"y"},
      {"Y"},
    };
  }

  @DataProvider
  public Object[][] booleanFalseValues() {
    return new Object[][]{
      {"false"},
      {"f"},
      {"0"},
      {"off"},
      {"no"},
      {"n"},
      {""},
      {"any other value"},
    };
  }

  @DataProvider
  public Object[][] primitiveTypesWithNullValues() {
    return new Object[][]{
      {boolean.class, false},
      {byte.class, (byte) 0},
      {short.class, (short) 0},
      {int.class, 0},
      {long.class, 0L},
      {float.class, 0F},
      {double.class, 0D},
      {char.class, (char) 0},
    };
  }

  @DataProvider
  public Object[][] boxedTypesWithNullValues() {
    return new Object[][]{
      {Boolean.class},
      {Byte.class},
      {Short.class},
      {Integer.class},
      {Long.class},
      {Float.class},
      {Double.class},
      {Character.class},
    };
  }

  @DataProvider
  public Object[][] bigDecimalZeroValues() {
    return new Object[][]{
      {null},
      {""},
      {" "},
      {"_"},
    };
  }

  @DataProvider
  public Object[][] charStandardSubstitutions() {
    return new Object[][]{
      {"\\n", char.class, '\n'},
      {"\\t", Character.class, '\t'},
      {"\\\\", char.class, '\\'},
      {"\\\"", Character.class, '"'},
      {"\\'", char.class, '\''},
    };
  }

  @DataProvider
  public Object[][] envValues() {
    return new Object[][]{
      {"ENV_STRING", "hello world", "$ENV{ENV_STRING}", String.class, "hello world"},
      {"ENV_BOOLEAN", "yes", "$ENV{ENV_BOOLEAN}", boolean.class, true},
      {"ENV_BOOLEAN_BOXED", "on", "$ENV{ENV_BOOLEAN_BOXED}", Boolean.class, true},
      {"ENV_BYTE", "1 2", "$ENV{ENV_BYTE}", byte.class, (byte) 12},
      {"ENV_BYTE_BOXED", "1_3", "$ENV{ENV_BYTE_BOXED}", Byte.class, (byte) 13},
      {"ENV_SHORT", "3 0 0", "$ENV{ENV_SHORT}", short.class, (short) 300},
      {"ENV_SHORT_BOXED", "3_0_1", "$ENV{ENV_SHORT_BOXED}", Short.class, (short) 301},
      {"ENV_INT", "123 456", "$ENV{ENV_INT}", int.class, 123456},
      {"ENV_INT_BOXED", "123_456.50", "$ENV{ENV_INT_BOXED}", Integer.class, 123457},
      {"ENV_LONG", "1 234 567 890 123", "$ENV{ENV_LONG}", long.class, 1234567890123L},
      {"ENV_LONG_BOXED", "1_234_567_890_123,50", "$ENV{ENV_LONG_BOXED}", Long.class, 1234567890124L},
      {"ENV_FLOAT", "3,25", "$ENV{ENV_FLOAT}", float.class, 3.25F},
      {"ENV_FLOAT_BOXED", "4,5", "$ENV{ENV_FLOAT_BOXED}", Float.class, 4.5F},
      {"ENV_DOUBLE", "6 000,75", "$ENV{ENV_DOUBLE}", double.class, 6000.75D},
      {"ENV_DOUBLE_BOXED", "7_000,125", "$ENV{ENV_DOUBLE_BOXED}", Double.class, 7000.125D},
      {"ENV_BIG_DECIMAL", "12 345_678,901", "$ENV{ENV_BIG_DECIMAL}", BigDecimal.class, new BigDecimal("12345678.901")},
      {"ENV_BIG_INTEGER", "12_345_678.50", "$ENV{ENV_BIG_INTEGER}", BigInteger.class, new BigInteger("12345679")},
      {"ENV_CHAR", "x", "$ENV{ENV_CHAR}", char.class, 'x'},
      {"ENV_CHAR_BOXED", "y", "$ENV{ENV_CHAR_BOXED}", Character.class, 'y'},
      {"ENV_STRING", "world", "hello $ENV{ENV_STRING}", String.class, "hello world"},
    };
  }

  @Test
  public void parseStrToGenericType__string() {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType("hello world", dynamicParams, String.class);
    //
    //

    assertThat(value).isInstanceOf(String.class);
    assertThat(value).isEqualTo("hello world");
  }

  @Test
  public void parseStrToGenericType__empty_string() {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType("", dynamicParams, String.class);
    //
    //

    assertThat(value).isInstanceOf(String.class);
    assertThat(value).isEqualTo("");
  }

  @Test
  public void parseStrToGenericType__null_string() {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(null, dynamicParams, String.class);
    //
    //

    assertThat(value).isNull();
  }

  @Test
  public void parseStrToGenericType__string__standard_substitutions() {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType("hello\\nworld\\t\\\\\\\"\\'", dynamicParams, String.class);
    //
    //

    assertThat(value).isInstanceOf(String.class);
    assertThat(value).isEqualTo("hello\nworld\t\\\"'");
  }

  @Test(dataProvider = "primitiveAndBoxedTypes")
  public void parseStrToGenericType__primitive_and_boxed_types(String valueStr, Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }

  @Test(dataProvider = "formattedNumericValues")
  public void parseStrToGenericType__formatted_numeric_values(String valueStr, Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }

  @Test(dataProvider = "numericValuesWithStandardSubstitutions")
  public void parseStrToGenericType__numeric_values_with_standard_substitutions(String valueStr, Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }

  @Test(dataProvider = "integerValuesWithDecimalParts")
  public void parseStrToGenericType__integer_values_with_decimal_parts_are_rounded(String valueStr, Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }

  @Test(dataProvider = "charStandardSubstitutions")
  public void parseStrToGenericType__char__standard_substitutions(String valueStr, Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }

  @Test(dataProvider = "booleanTrueValues")
  public void parseStrToGenericType__boolean_true_values(String valueStr) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object primitiveValue = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, boolean.class);
    Object boxedValue     = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, Boolean.class);
    //
    //

    assertThat(primitiveValue).isEqualTo(true);
    assertThat(boxedValue).isEqualTo(true);
  }

  @Test(dataProvider = "booleanFalseValues")
  public void parseStrToGenericType__boolean_false_values(String valueStr) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object primitiveValue = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, boolean.class);
    Object boxedValue     = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, Boolean.class);
    //
    //

    assertThat(primitiveValue).isEqualTo(false);
    assertThat(boxedValue).isEqualTo(false);
  }

  @Test(dataProvider = "primitiveTypesWithNullValues")
  public void parseStrToGenericType__primitive_types__null_value(Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(null, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }

  @Test(dataProvider = "boxedTypesWithNullValues")
  public void parseStrToGenericType__boxed_types__null_value(Type type) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(null, dynamicParams, type);
    //
    //

    assertThat(value).isNull();
  }

  @Test(dataProvider = "bigDecimalZeroValues")
  public void parseStrToGenericType__big_decimal__blank_or_null_value(String valueStr) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, BigDecimal.class);
    //
    //

    assertThat(value).isEqualTo(BigDecimal.ZERO);
    assertThat(value).isInstanceOf(BigDecimal.class);
  }

  @Test(dataProvider = "envValues")
  public void parseStrToGenericType__env_values(String envName, String envValue, String valueStr, Type type, Object expectedValue) {

    DynamicParamsFake dynamicParams = new DynamicParamsFake(13);
    dynamicParams.envMap.put(envName, envValue);

    //
    //
    Object value = ParseUtil.parseStrToGenericType(valueStr, dynamicParams, type);
    //
    //

    assertThat(value).isEqualTo(expectedValue);
    assertThat(value).isInstanceOf(expectedValue.getClass());
  }
}
