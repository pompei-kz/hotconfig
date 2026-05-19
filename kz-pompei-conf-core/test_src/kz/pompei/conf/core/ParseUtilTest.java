package kz.pompei.conf.core;

import java.lang.reflect.Type;
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
}
