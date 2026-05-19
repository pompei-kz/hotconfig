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
