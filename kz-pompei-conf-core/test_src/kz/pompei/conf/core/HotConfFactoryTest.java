package kz.pompei.conf.core;

import java.util.List;
import kz.pompei.conf.core.ann.ConfDefaultValue;
import kz.pompei.conf.core.ann.ConfDoc;
import kz.pompei.conf.core.ann.ConfFolder;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import kz.pompei.conf.core.model.HotConfFactoryParams;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HotConfFactoryTest {

  @ConfDoc("about1\nabout2\nabout3")
  @ConfFolder("cool/folder")
  public interface TestConf1 {

    @ConfDoc("description1\ndescription2")
    @ConfDefaultValue("def value 1")
    String param1();

    @ConfDoc("description3\ndescription4\ndescription5")
    @ConfDefaultValue("def value 2")
    String param2();

  }

  @ConfDoc("ABOUT1\nABOUT2\nABOUT3")
  public interface TestConf2 {

    @ConfDoc("DESCRIPTION1\nDESCRIPTION2")
    @ConfDefaultValue("DEF VALUE 1")
    String status1();

    @ConfDoc("DESCRIPTION3\nDESCRIPTION4\nDESCRIPTION5")
    @ConfDefaultValue("DEF VALUE 2")
    String status2();
  }

  @Test
  public void createConf__fewParams() {

    Conf conf = new Conf();
    conf.params.add(new ConfParam("status1", "DIRECT VALUE"));
    conf.params.add(new ConfParam("left-param", "come value").comment("line1\nline2"));

    String confPath2 = "TestConf2.tst";

    ConfTunnelFake tunnel = new ConfTunnelFake();

    tunnel.storage.put(confPath2, new ConfTunnelFake.Dot(conf, 1));

    DynamicParamsFake dynamicParams          = new DynamicParamsFake(13);
    int               revisionCheckTimeoutMs = 500;

    HotConfFactoryParams params = HotConfFactoryParams.builder()
                                                      .extension(".tst")
                                                      .revisionCheckTimeoutMs(revisionCheckTimeoutMs)
                                                      .build();

    HotConfFactory factory = new HotConfFactory(tunnel, params, dynamicParams);

    //
    //
    TestConf2 testConf2 = factory.createConf(TestConf2.class);
    //
    //

    assertThat(testConf2).isNotNull();

    String status1 = testConf2.status1();
    String status2 = testConf2.status2();

    assertThat(status1).isEqualTo("DIRECT VALUE");
    assertThat(status2).isEqualTo("DEF VALUE 2");

    Conf      conf2 = tunnel.storage.get(confPath2).conf();
    ConfParam p0    = conf2.params.get(0);
    ConfParam p1    = conf2.params.get(1);
    ConfParam p2    = conf2.params.get(2);

    assertThat(p0.name).isEqualTo("status1");
    assertThat(p1.name).describedAs("3q1jRa8Dt0 :: it should not be removed").isEqualTo("left-param");
    assertThat(p2.name).isEqualTo("status2");
  }

  @Test
  public void createConf__createsConfigFile() {

    ConfTunnelFake    tunnel                 = new ConfTunnelFake();
    DynamicParamsFake dynamicParams          = new DynamicParamsFake(13);
    int               revisionCheckTimeoutMs = 500;

    HotConfFactoryParams params = HotConfFactoryParams.builder()
                                                      .extension(".tst")
                                                      .revisionCheckTimeoutMs(revisionCheckTimeoutMs)
                                                      .build();

    HotConfFactory factory = new HotConfFactory(tunnel, params, dynamicParams);

    //
    //
    TestConf1 conf1 = factory.createConf(TestConf1.class);
    TestConf2 conf2 = factory.createConf(TestConf2.class);
    //
    //

    assertThat(conf1).isNotNull();
    assertThat(conf2).isNotNull();
    assertThat(tunnel.storage).isEmpty();
    tunnel.readCount_isEmpty();
    tunnel.writeCount_isEmpty();
    tunnel.modificationMarkerCount_isEmpty();

    String confPath1 = "cool/folder/TestConf1.tst";
    String confPath2 = "TestConf2.tst";

    {
      tunnel.clearCounts();

      String param1 = conf1.param1();

      assertThat(tunnel.modificationMarkerCount(confPath1)).isGreaterThanOrEqualTo(1);

      String param2 = conf1.param2();

      assertThat(tunnel.modificationMarkerCount(confPath1)).isGreaterThanOrEqualTo(1);

      assertThat(param1).isEqualTo("def value 1");
      assertThat(param2).isEqualTo("def value 2");

      assertThat(tunnel.readCount(confPath1)).isEqualTo(0);
      assertThat(tunnel.writeCount(confPath1)).isEqualTo(1);
    }

    {
      ConfTunnelFake.Dot dot  = tunnel.storage.get(confPath1);
      Conf               conf = dot.conf();
      assertThat(conf).isNotNull();
      assertThat(conf.confComments).isEqualTo(List.of("about1", "about2", "about3"));
      assertThat(conf.params.get(0).comments).isEqualTo(List.of("description1", "description2"));
      assertThat(conf.params.get(1).comments).isEqualTo(List.of("description3", "description4", "description5"));

      assertThat(conf.params.get(0).name).isEqualTo("param1");
      assertThat(conf.params.get(1).name).isEqualTo("param2");

      assertThat(conf.params.get(0).valueStr).isEqualTo("def value 1");
      assertThat(conf.params.get(1).valueStr).isEqualTo("def value 2");
    }

    {
      tunnel.clearCounts();

      String status1 = conf2.status1();

      assertThat(tunnel.modificationMarkerCount(confPath2)).isGreaterThanOrEqualTo(1);

      String status2 = conf2.status2();

      assertThat(tunnel.modificationMarkerCount(confPath2)).isGreaterThanOrEqualTo(1);

      assertThat(status1).isEqualTo("DEF VALUE 1");
      assertThat(status2).isEqualTo("DEF VALUE 2");

      assertThat(tunnel.readCount(confPath2)).isEqualTo(0);
      assertThat(tunnel.writeCount(confPath2)).isEqualTo(1);
    }

    /*
     * Less time has passed than 'revisionCheckTimeoutMs',
     * so there was no need to check the changes in the config file
     */
    dynamicParams.inc(revisionCheckTimeoutMs - 100);

    {
      tunnel.clearCounts();

      String param1 = conf1.param1();
      String param2 = conf1.param2();
      assertThat(param1).isEqualTo("def value 1");
      assertThat(param2).isEqualTo("def value 2");

      assertThat(tunnel.readCount(confPath1)).isZero();
      assertThat(tunnel.writeCount(confPath1)).isZero();
      assertThat(tunnel.modificationMarkerCount(confPath1))
        .describedAs("bqmJgokq4Q :: the system shouldn't call `modificationMarker`")
        .isZero();
    }

    /*
     * Now enough time has passed for to call `modificationMarker`
     */
    dynamicParams.inc(100 + 10);

    {
      tunnel.clearCounts();

      String param1 = conf1.param1();
      String param2 = conf1.param2();
      assertThat(param1).isEqualTo("def value 1");
      assertThat(param2).isEqualTo("def value 2");

      assertThat(tunnel.readCount(confPath1))
        .describedAs("UVe1kq1i1x :: Must be zero, because configFile didn't change").isEqualTo(0);
      assertThat(tunnel.writeCount(confPath1)).isEqualTo(0);
      assertThat(tunnel.modificationMarkerCount(confPath1))
        .describedAs("dc6QP1Q1BR :: the system should call `modificationMarker` because enough time has passed")
        .isGreaterThanOrEqualTo(1);
    }


    { // now let's change some value in the config file

      ConfTunnelFake.Dot dot  = tunnel.storage.get(confPath1);
      Conf               conf = dot.conf().copy();
      assertThat(conf).isNotNull();
      assertThat(conf.confComments).isEqualTo(List.of("about1", "about2", "about3"));

      ConfParam cp0 = conf.params.get(0);
      ConfParam cp1 = conf.params.get(1);

      assertThat(cp0.name).isEqualTo("param1");
      assertThat(cp1.name).isEqualTo("param2");

      cp0.valueStr = "new value 1";
      cp1.valueStr = "new value 2";

      tunnel.storage.put(confPath1, new ConfTunnelFake.Dot(conf, dot.revision() + 1));
    }

    /*
     * First, let's check that not enough time has passed.
     */
    dynamicParams.inc(revisionCheckTimeoutMs - 100);

    {
      tunnel.clearCounts();

      String param1 = conf1.param1();
      String param2 = conf1.param2();
      assertThat(param1).describedAs("4t9MTx7dZl :: Must be old value").isEqualTo("def value 1");
      assertThat(param2).describedAs("gRWeeGMa6O :: Must be old value").isEqualTo("def value 2");

      assertThat(tunnel.readCount(confPath1)).describedAs("yNn1hPPRUs :: Cannot be called").isZero();
      assertThat(tunnel.writeCount(confPath1)).isZero();
      assertThat(tunnel.modificationMarkerCount(confPath1)).describedAs("b46Nx1LV1t :: Cannot be called").isZero();
    }

    /*
     * And enough time has passed for to call `modificationMarker`
     */
    dynamicParams.inc(100 + 10);

    {
      tunnel.clearCounts();

      String param1 = conf1.param1();
      String param2 = conf1.param2();
      assertThat(param1).describedAs("ZqPrcTBP2A :: Must be NEW value").isEqualTo("new value 1");
      assertThat(param2).describedAs("2LDtWNUsKS :: Must be NEW value").isEqualTo("new value 2");

      assertThat(tunnel.readCount(confPath1)).describedAs("ycWUaL1LHy :: must be read").isEqualTo(1);
      assertThat(tunnel.writeCount(confPath1)).isZero();
      assertThat(tunnel.modificationMarkerCount(confPath1)).describedAs("rMFGcMExaL :: must be read").isGreaterThanOrEqualTo(1);
    }

    assertThat(tunnel.readCount(confPath2)).describedAs("ycWUaL1LHy :: must be read").isZero();
    assertThat(tunnel.writeCount(confPath2)).isZero();
    assertThat(tunnel.modificationMarkerCount(confPath2)).describedAs("rMFGcMExaL :: must be read").isZero();
  }
}
