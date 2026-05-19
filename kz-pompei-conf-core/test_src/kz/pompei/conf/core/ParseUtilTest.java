package kz.pompei.conf.core;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParseUtilTest {

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

    //noinspection ConstantValue
    assertThat(value).isNull();
  }
}
