package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import kz.pompei.hotconfig.jdbc.tst_utils.JdbcTestDbUtils;
import lombok.NonNull;
import org.testng.annotations.Test;
import utils.RND;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTunnelJdbcTest extends JdbcTestDbUtils {

  @Test(dataProvider = "databaseType")
  public void checkDbConnection(@NonNull DatabaseType databaseType) throws SQLException {

    ConnectionGet connectionGet = createConnectionGet(databaseType, "read");

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("SELECT 1")) {
        try (ResultSet rs = ps.executeQuery()) {
          rs.next();
          if (rs.getInt(1) != 1) {
            throw new SQLException("S1t2U3v4W5 :: Connection OK");
          }
        }
      }
    }

  }

  @Test(dataProvider = "databaseType")
  public void read_existsInDbTable(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_existsInDbTable";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    createTable(connectionGet, def);
    clearTable(connectionGet, def.tableName);

    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "", "", "This is comment for conf\nline 2\nline 3");
    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "param0", "value0", "This is test0\nsecond line\nanother line");
    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "param1", "value1", "This is test1\nsecond line\nanother line");

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    Conf conf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    //
    //

    assertThat(conf).isNotNull();

    assertThat(conf.confComments).isEqualTo(List.of("This is comment for conf", "line 2", "line 3"));

    ConfParam param0 = conf.params.get(0);// params sorted by name
    ConfParam param1 = conf.params.get(1);

    assertThat(param0.name).isEqualTo("param0");
    assertThat(param1.name).isEqualTo("param1");

    assertThat(param0.valueStr).isEqualTo("value0");
    assertThat(param1.valueStr).isEqualTo("value1");

    assertThat(param0.comments).isEqualTo(List.of("This is test0", "second line", "another line"));
    assertThat(param1.comments).isEqualTo(List.of("This is test1", "second line", "another line"));
  }

  @Test(dataProvider = "databaseType")
  public void read_existsInDbTable_withThreeConfigsAndMultilineComments(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_exists_three_cfgs";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    createTable(connectionGet, def);
    clearTable(connectionGet, def.tableName);

    insertRow(
      connectionGet,
      def,
      "some/folder",
      "first-config.hotconf",
      "",
      "",
      "first config comment 1\nfirst config comment 2\nfirst config comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "some/folder",
      "first-config.hotconf",
      "alpha",
      "alpha-value",
      "alpha comment 1\nalpha comment 2\nalpha comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "some/folder",
      "first-config.hotconf",
      "beta",
      "beta-value",
      "beta comment 1\nbeta comment 2\nbeta comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "some/folder",
      "first-config.hotconf",
      "gamma",
      "gamma-value",
      "gamma comment 1\ngamma comment 2\ngamma comment 3"
    );

    insertRow(
      connectionGet,
      def,
      "some/folder",
      "second-config.hotconf",
      "",
      "",
      "second config comment 1\nsecond config comment 2\nsecond config comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "some/folder",
      "second-config.hotconf",
      "delta",
      "delta-value",
      "delta comment 1\ndelta comment 2\ndelta comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "some/folder",
      "second-config.hotconf",
      "epsilon",
      "epsilon-value",
      "epsilon comment 1\nepsilon comment 2\nepsilon comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "some/folder",
      "second-config.hotconf",
      "zeta",
      "zeta-value",
      "zeta comment 1\nzeta comment 2\nzeta comment 3"
    );

    insertRow(
      connectionGet,
      def,
      "other/folder",
      "third-config.hotconf",
      "",
      "",
      "third config comment 1\nthird config comment 2\nthird config comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "other/folder",
      "third-config.hotconf",
      "eta",
      "eta-value",
      "eta comment 1\neta comment 2\neta comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "other/folder",
      "third-config.hotconf",
      "theta",
      "theta-value",
      "theta comment 1\ntheta comment 2\ntheta comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "other/folder",
      "third-config.hotconf",
      "iota",
      "iota-value",
      "iota comment 1\niota comment 2\niota comment 3"
    );

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    Conf firstConf  = confTunnelJdbc.read("some/folder/first-config.hotconf");
    Conf secondConf = confTunnelJdbc.read("some/folder/second-config.hotconf");
    Conf thirdConf  = confTunnelJdbc.read("other/folder/third-config.hotconf");
    //
    //

    assertThat(firstConf).isNotNull();
    assertThat(firstConf.confComments).isEqualTo(List.of(
      "first config comment 1",
      "first config comment 2",
      "first config comment 3"
    ));
    ConfParam firstAlpha = firstConf.params.get(0);// params sorted by name
    ConfParam firstBeta  = firstConf.params.get(1);
    ConfParam firstGamma = firstConf.params.get(2);
    assertThat(firstAlpha.name).isEqualTo("alpha");
    assertThat(firstBeta.name).isEqualTo("beta");
    assertThat(firstGamma.name).isEqualTo("gamma");
    assertThat(firstAlpha.valueStr).isEqualTo("alpha-value");
    assertThat(firstBeta.valueStr).isEqualTo("beta-value");
    assertThat(firstGamma.valueStr).isEqualTo("gamma-value");
    assertThat(firstAlpha.comments).isEqualTo(List.of("alpha comment 1", "alpha comment 2", "alpha comment 3"));
    assertThat(firstBeta.comments).isEqualTo(List.of("beta comment 1", "beta comment 2", "beta comment 3"));
    assertThat(firstGamma.comments).isEqualTo(List.of("gamma comment 1", "gamma comment 2", "gamma comment 3"));

    assertThat(secondConf).isNotNull();
    assertThat(secondConf.confComments).isEqualTo(List.of(
      "second config comment 1",
      "second config comment 2",
      "second config comment 3"
    ));
    ConfParam secondDelta   = secondConf.params.get(0);// params sorted by name
    ConfParam secondEpsilon = secondConf.params.get(1);
    ConfParam secondZeta    = secondConf.params.get(2);
    assertThat(secondDelta.name).isEqualTo("delta");
    assertThat(secondEpsilon.name).isEqualTo("epsilon");
    assertThat(secondZeta.name).isEqualTo("zeta");
    assertThat(secondDelta.valueStr).isEqualTo("delta-value");
    assertThat(secondEpsilon.valueStr).isEqualTo("epsilon-value");
    assertThat(secondZeta.valueStr).isEqualTo("zeta-value");
    assertThat(secondDelta.comments).isEqualTo(List.of("delta comment 1", "delta comment 2", "delta comment 3"));
    assertThat(secondEpsilon.comments).isEqualTo(List.of("epsilon comment 1", "epsilon comment 2", "epsilon comment 3"));
    assertThat(secondZeta.comments).isEqualTo(List.of("zeta comment 1", "zeta comment 2", "zeta comment 3"));

    assertThat(thirdConf).isNotNull();
    assertThat(thirdConf.confComments).isEqualTo(List.of(
      "third config comment 1",
      "third config comment 2",
      "third config comment 3"
    ));
    ConfParam thirdEta   = thirdConf.params.get(0);// params sorted by name
    ConfParam thirdIota  = thirdConf.params.get(1);
    ConfParam thirdTheta = thirdConf.params.get(2);
    assertThat(thirdEta.name).isEqualTo("eta");
    assertThat(thirdIota.name).isEqualTo("iota");
    assertThat(thirdTheta.name).isEqualTo("theta");
    assertThat(thirdEta.valueStr).isEqualTo("eta-value");
    assertThat(thirdIota.valueStr).isEqualTo("iota-value");
    assertThat(thirdTheta.valueStr).isEqualTo("theta-value");
    assertThat(thirdEta.comments).isEqualTo(List.of("eta comment 1", "eta comment 2", "eta comment 3"));
    assertThat(thirdIota.comments).isEqualTo(List.of("iota comment 1", "iota comment 2", "iota comment 3"));
    assertThat(thirdTheta.comments).isEqualTo(List.of("theta comment 1", "theta comment 2", "theta comment 3"));
  }

  @Test(dataProvider = "databaseType")
  public void read_tableDoesNotExists(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_tableDoesNotExists";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    assertThat(tableExists(connectionGet, def.tableName)).isFalse();

    //
    //
    Conf conf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    //
    //

    assertThat(conf).isNull();
    assertThat(tableExists(connectionGet, def.tableName)).isFalse();
  }

  @Test(dataProvider = "databaseType")
  public void write_tableDoesNotExists(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "write_tableDoesNotExists";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    Conf conf = new Conf();
    conf.confComments.add("This is comment for conf");
    conf.confComments.add("line 2");
    conf.confComments.add("line 3");

    ConfParam param0 = new ConfParam();
    param0.comments.add("This is test0");
    param0.comments.add("second line");
    param0.comments.add("another line");
    param0.name     = "param0";
    param0.valueStr = "value0";
    conf.params.add(param0);

    ConfParam param1 = new ConfParam();
    param1.comments.add("This is test1");
    param1.comments.add("second line");
    param1.comments.add("another line");
    param1.name     = "param1";
    param1.valueStr = "value1";
    conf.params.add(param1);

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);
    //
    //

    Conf readConf = confTunnelJdbc.read("some/folder/test-config.hotconf");

    assertThat(readConf).isNotNull();
    assertThat(readConf.confComments).isEqualTo(List.of("This is comment for conf", "line 2", "line 3"));

    ConfParam readParam0 = readConf.params.get(0);// params sorted by name
    ConfParam readParam1 = readConf.params.get(1);

    assertThat(readParam0.name).isEqualTo("param0");
    assertThat(readParam1.name).isEqualTo("param1");

    assertThat(readParam0.valueStr).isEqualTo("value0");
    assertThat(readParam1.valueStr).isEqualTo("value1");

    assertThat(readParam0.comments).isEqualTo(List.of("This is test0", "second line", "another line"));
    assertThat(readParam1.comments).isEqualTo(List.of("This is test1", "second line", "another line"));
  }

  @Test(dataProvider = "databaseType")
  public void write_savesTwoConfigsWithMultilineComments(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "write_savesTwoConfigsWithMultilineComments";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    Conf firstConf = new Conf();
    firstConf.confComments.add("first config comment 1");
    firstConf.confComments.add("first config comment 2");
    firstConf.confComments.add("first config comment 3");

    ConfParam firstAlpha = new ConfParam();
    firstAlpha.comments.add("alpha comment 1");
    firstAlpha.comments.add("alpha comment 2");
    firstAlpha.comments.add("alpha comment 3");
    firstAlpha.name     = "alpha";
    firstAlpha.valueStr = "alpha-value";
    firstConf.params.add(firstAlpha);

    ConfParam firstBeta = new ConfParam();
    firstBeta.comments.add("beta comment 1");
    firstBeta.comments.add("beta comment 2");
    firstBeta.comments.add("beta comment 3");
    firstBeta.name     = "beta";
    firstBeta.valueStr = "beta-value";
    firstConf.params.add(firstBeta);

    ConfParam firstGamma = new ConfParam();
    firstGamma.comments.add("gamma comment 1");
    firstGamma.comments.add("gamma comment 2");
    firstGamma.comments.add("gamma comment 3");
    firstGamma.name     = "gamma";
    firstGamma.valueStr = "gamma-value";
    firstConf.params.add(firstGamma);

    Conf secondConf = new Conf();
    secondConf.confComments.add("second config comment 1");
    secondConf.confComments.add("second config comment 2");
    secondConf.confComments.add("second config comment 3");

    ConfParam secondDelta = new ConfParam();
    secondDelta.comments.add("delta comment 1");
    secondDelta.comments.add("delta comment 2");
    secondDelta.comments.add("delta comment 3");
    secondDelta.name     = "delta";
    secondDelta.valueStr = "delta-value";
    secondConf.params.add(secondDelta);

    ConfParam secondEpsilon = new ConfParam();
    secondEpsilon.comments.add("epsilon comment 1");
    secondEpsilon.comments.add("epsilon comment 2");
    secondEpsilon.comments.add("epsilon comment 3");
    secondEpsilon.name     = "epsilon";
    secondEpsilon.valueStr = "epsilon-value";
    secondConf.params.add(secondEpsilon);

    ConfParam secondZeta = new ConfParam();
    secondZeta.comments.add("zeta comment 1");
    secondZeta.comments.add("zeta comment 2");
    secondZeta.comments.add("zeta comment 3");
    secondZeta.name     = "zeta";
    secondZeta.valueStr = "zeta-value";
    secondConf.params.add(secondZeta);

    Conf thirdConf = new Conf();
    thirdConf.confComments.add("third config comment 1");
    thirdConf.confComments.add("third config comment 2");
    thirdConf.confComments.add("third config comment 3");

    ConfParam thirdEta = new ConfParam();
    thirdEta.comments.add("eta comment 1");
    thirdEta.comments.add("eta comment 2");
    thirdEta.comments.add("eta comment 3");
    thirdEta.name     = "eta";
    thirdEta.valueStr = "eta-value";
    thirdConf.params.add(thirdEta);

    ConfParam thirdTheta = new ConfParam();
    thirdTheta.comments.add("theta comment 1");
    thirdTheta.comments.add("theta comment 2");
    thirdTheta.comments.add("theta comment 3");
    thirdTheta.name     = "theta";
    thirdTheta.valueStr = "theta-value";
    thirdConf.params.add(thirdTheta);

    ConfParam thirdIota = new ConfParam();
    thirdIota.comments.add("iota comment 1");
    thirdIota.comments.add("iota comment 2");
    thirdIota.comments.add("iota comment 3");
    thirdIota.name     = "iota";
    thirdIota.valueStr = "iota-value";
    thirdConf.params.add(thirdIota);

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    confTunnelJdbc.write("some/folder/first-config.hotconf", firstConf);
    confTunnelJdbc.write("some/folder/second-config.hotconf", secondConf);
    confTunnelJdbc.write("other/folder/third-config.hotconf", thirdConf);
    //
    //

    Conf readFirstConf  = confTunnelJdbc.read("some/folder/first-config.hotconf");
    Conf readSecondConf = confTunnelJdbc.read("some/folder/second-config.hotconf");
    Conf readThirdConf  = confTunnelJdbc.read("other/folder/third-config.hotconf");

    assertThat(readFirstConf).isNotNull();
    assertThat(readFirstConf.confComments).isEqualTo(List.of(
      "first config comment 1",
      "first config comment 2",
      "first config comment 3"
    ));

    ConfParam readFirstAlpha = readFirstConf.params.get(0);// params sorted by name
    ConfParam readFirstBeta  = readFirstConf.params.get(1);
    ConfParam readFirstGamma = readFirstConf.params.get(2);

    assertThat(readFirstAlpha.name).isEqualTo("alpha");
    assertThat(readFirstBeta.name).isEqualTo("beta");
    assertThat(readFirstGamma.name).isEqualTo("gamma");

    assertThat(readFirstAlpha.valueStr).isEqualTo("alpha-value");
    assertThat(readFirstBeta.valueStr).isEqualTo("beta-value");
    assertThat(readFirstGamma.valueStr).isEqualTo("gamma-value");

    assertThat(readFirstAlpha.comments).isEqualTo(List.of("alpha comment 1", "alpha comment 2", "alpha comment 3"));
    assertThat(readFirstBeta.comments).isEqualTo(List.of("beta comment 1", "beta comment 2", "beta comment 3"));
    assertThat(readFirstGamma.comments).isEqualTo(List.of("gamma comment 1", "gamma comment 2", "gamma comment 3"));

    assertThat(readSecondConf).isNotNull();
    assertThat(readSecondConf.confComments).isEqualTo(List.of(
      "second config comment 1",
      "second config comment 2",
      "second config comment 3"
    ));

    ConfParam readSecondDelta   = readSecondConf.params.get(0);// params sorted by name
    ConfParam readSecondEpsilon = readSecondConf.params.get(1);
    ConfParam readSecondZeta    = readSecondConf.params.get(2);

    assertThat(readSecondDelta.name).isEqualTo("delta");
    assertThat(readSecondEpsilon.name).isEqualTo("epsilon");
    assertThat(readSecondZeta.name).isEqualTo("zeta");

    assertThat(readSecondDelta.valueStr).isEqualTo("delta-value");
    assertThat(readSecondEpsilon.valueStr).isEqualTo("epsilon-value");
    assertThat(readSecondZeta.valueStr).isEqualTo("zeta-value");

    assertThat(readSecondDelta.comments).isEqualTo(List.of("delta comment 1", "delta comment 2", "delta comment 3"));
    assertThat(readSecondEpsilon.comments).isEqualTo(List.of("epsilon comment 1", "epsilon comment 2", "epsilon comment 3"));
    assertThat(readSecondZeta.comments).isEqualTo(List.of("zeta comment 1", "zeta comment 2", "zeta comment 3"));

    assertThat(readThirdConf).isNotNull();
    assertThat(readThirdConf.confComments).isEqualTo(List.of(
      "third config comment 1",
      "third config comment 2",
      "third config comment 3"
    ));

    ConfParam readThirdEta   = readThirdConf.params.get(0);// params sorted by name
    ConfParam readThirdIota  = readThirdConf.params.get(1);
    ConfParam readThirdTheta = readThirdConf.params.get(2);

    assertThat(readThirdEta.name).isEqualTo("eta");
    assertThat(readThirdTheta.name).isEqualTo("theta");
    assertThat(readThirdIota.name).isEqualTo("iota");

    assertThat(readThirdEta.valueStr).isEqualTo("eta-value");
    assertThat(readThirdTheta.valueStr).isEqualTo("theta-value");
    assertThat(readThirdIota.valueStr).isEqualTo("iota-value");

    assertThat(readThirdEta.comments).isEqualTo(List.of("eta comment 1", "eta comment 2", "eta comment 3"));
    assertThat(readThirdTheta.comments).isEqualTo(List.of("theta comment 1", "theta comment 2", "theta comment 3"));
    assertThat(readThirdIota.comments).isEqualTo(List.of("iota comment 1", "iota comment 2", "iota comment 3"));
  }

  @Test(dataProvider = "databaseType")
  public void modificationMarker_updatesOnRowChange(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "lastModified_updatesOnRowChange";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    createTable(connectionGet, def);
    clearTable(connectionGet, def.tableName);

    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    Long initialLastModified = confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    waitForChange(databaseType);
    updateRow(connectionGet, def, "some/folder", "test-config.hotconf", "", "changed", "This is comment for conf");

    //
    //
    Long updatedLastModified = confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    assertThat(initialLastModified).isNotNull();
    assertThat(updatedLastModified).isNotNull();
    assertThat(updatedLastModified).isGreaterThan(initialLastModified);
  }

  @Test(dataProvider = "databaseType")
  public void modificationMarker_updatesOnRowChange_withTwoParams(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "modMarker_two_params";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    createTable(connectionGet, def);
    clearTable(connectionGet, def.tableName);

    insertRow(
      connectionGet,
      def,
      "some/folder",
      "first-config.hotconf",
      "alpha",
      "alpha-value",
      "alpha comment 1\nalpha comment 2\nalpha comment 3"
    );
    insertRow(
      connectionGet,
      def,
      "other/folder",
      "second-config.hotconf",
      "beta",
      "beta-value",
      "beta comment 1\nbeta comment 2\nbeta comment 3"
    );

    ConfigTunnelJdbc confTunnelJdbc = ConfigTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    Long initialFirstMarker  = confTunnelJdbc.modificationMarker("some/folder/first-config.hotconf");
    Long initialSecondMarker = confTunnelJdbc.modificationMarker("other/folder/second-config.hotconf");
    //
    //

    waitForChange(databaseType);
    updateRow(
      connectionGet,
      def,
      "other/folder",
      "second-config.hotconf",
      "beta",
      "beta-value-changed",
      "beta comment 1\nbeta comment 2\nbeta comment 3"
    );
    updateRow(
      connectionGet,
      def,
      "some/folder",
      "first-config.hotconf",
      "alpha",
      "alpha-value-changed",
      "alpha comment 1\nalpha comment 2\nalpha comment 3"
    );

    //
    //
    Long updatedFirstMarker  = confTunnelJdbc.modificationMarker("some/folder/first-config.hotconf");
    Long updatedSecondMarker = confTunnelJdbc.modificationMarker("other/folder/second-config.hotconf");
    //
    //

    assertThat(initialFirstMarker).isNotNull();
    assertThat(initialSecondMarker).isNotNull();
    assertThat(updatedFirstMarker).isNotNull();
    assertThat(updatedSecondMarker).isNotNull();

    assertThat(updatedFirstMarker).isGreaterThan(initialFirstMarker);
    assertThat(updatedSecondMarker).isGreaterThan(initialSecondMarker);
  }

}
