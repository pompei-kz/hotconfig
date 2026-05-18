package kz.pompei.conf.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import kz.pompei.conf.jdbc.tst_utils.JdbcTestDbUtils;
import lombok.NonNull;
import org.testng.annotations.Test;
import utils.RND;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfTunnelJdbcTest extends JdbcTestDbUtils {

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

    ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    createTable(connectionGet, def);
    clearTable(connectionGet, def.tableName);

    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "", "", "This is comment for conf\nline 2\nline 3");
    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "param0", "value0", "This is test0\nsecond line\nanother line");
    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "param1", "value1", "This is test1\nsecond line\nanother line");

    ConfTunnelJdbc confTunnelJdbc = ConfTunnelJdbcBuilder.build(connectionGet, def);

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
  public void read_tableDoesNotExists(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_tableDoesNotExists";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    ConfTunnelJdbc confTunnelJdbc = ConfTunnelJdbcBuilder.build(connectionGet, def);

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

    ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    Conf conf = new Conf();
    conf.confComments.add("This is comment for conf");
    conf.confComments.add("line 2");
    conf.confComments.add("line 3");

    ConfParam param0 = new ConfParam();
    param0.comments.add("This is test0");
    param0.comments.add("second line");
    param0.comments.add("another line");
    param0.name = "param0";
    param0.valueStr = "value0";
    conf.params.add(param0);

    ConfParam param1 = new ConfParam();
    param1.comments.add("This is test1");
    param1.comments.add("second line");
    param1.comments.add("another line");
    param1.name = "param1";
    param1.valueStr = "value1";
    conf.params.add(param1);

    ConfTunnelJdbc confTunnelJdbc = ConfTunnelJdbcBuilder.build(connectionGet, def);

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

    ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    Conf firstConf = new Conf();
    firstConf.confComments.add("first config comment 1");
    firstConf.confComments.add("first config comment 2");
    firstConf.confComments.add("first config comment 3");

    ConfParam firstAlpha = new ConfParam();
    firstAlpha.comments.add("alpha comment 1");
    firstAlpha.comments.add("alpha comment 2");
    firstAlpha.comments.add("alpha comment 3");
    firstAlpha.name = "alpha";
    firstAlpha.valueStr = "alpha-value";
    firstConf.params.add(firstAlpha);

    ConfParam firstBeta = new ConfParam();
    firstBeta.comments.add("beta comment 1");
    firstBeta.comments.add("beta comment 2");
    firstBeta.comments.add("beta comment 3");
    firstBeta.name = "beta";
    firstBeta.valueStr = "beta-value";
    firstConf.params.add(firstBeta);

    ConfParam firstGamma = new ConfParam();
    firstGamma.comments.add("gamma comment 1");
    firstGamma.comments.add("gamma comment 2");
    firstGamma.comments.add("gamma comment 3");
    firstGamma.name = "gamma";
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
    secondDelta.name = "delta";
    secondDelta.valueStr = "delta-value";
    secondConf.params.add(secondDelta);

    ConfParam secondEpsilon = new ConfParam();
    secondEpsilon.comments.add("epsilon comment 1");
    secondEpsilon.comments.add("epsilon comment 2");
    secondEpsilon.comments.add("epsilon comment 3");
    secondEpsilon.name = "epsilon";
    secondEpsilon.valueStr = "epsilon-value";
    secondConf.params.add(secondEpsilon);

    ConfParam secondZeta = new ConfParam();
    secondZeta.comments.add("zeta comment 1");
    secondZeta.comments.add("zeta comment 2");
    secondZeta.comments.add("zeta comment 3");
    secondZeta.name = "zeta";
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
    thirdEta.name = "eta";
    thirdEta.valueStr = "eta-value";
    thirdConf.params.add(thirdEta);

    ConfParam thirdTheta = new ConfParam();
    thirdTheta.comments.add("theta comment 1");
    thirdTheta.comments.add("theta comment 2");
    thirdTheta.comments.add("theta comment 3");
    thirdTheta.name = "theta";
    thirdTheta.valueStr = "theta-value";
    thirdConf.params.add(thirdTheta);

    ConfParam thirdIota = new ConfParam();
    thirdIota.comments.add("iota comment 1");
    thirdIota.comments.add("iota comment 2");
    thirdIota.comments.add("iota comment 3");
    thirdIota.name = "iota";
    thirdIota.valueStr = "iota-value";
    thirdConf.params.add(thirdIota);

    ConfTunnelJdbc confTunnelJdbc = ConfTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    confTunnelJdbc.write("some/folder/first-config.hotconf", firstConf);
    confTunnelJdbc.write("some/folder/second-config.hotconf", secondConf);
    confTunnelJdbc.write("other/folder/third-config.hotconf", thirdConf);
    //
    //

    Conf readFirstConf = confTunnelJdbc.read("some/folder/first-config.hotconf");
    Conf readSecondConf = confTunnelJdbc.read("some/folder/second-config.hotconf");
    Conf readThirdConf = confTunnelJdbc.read("other/folder/third-config.hotconf");

    assertThat(readFirstConf).isNotNull();
    assertThat(readFirstConf.confComments).isEqualTo(List.of(
      "first config comment 1",
      "first config comment 2",
      "first config comment 3"
    ));

    ConfParam readFirstAlpha = readFirstConf.params.get(0);// params sorted by name
    ConfParam readFirstBeta = readFirstConf.params.get(1);
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

    ConfParam readSecondDelta = readSecondConf.params.get(0);// params sorted by name
    ConfParam readSecondEpsilon = readSecondConf.params.get(1);
    ConfParam readSecondZeta = readSecondConf.params.get(2);

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

    ConfParam readThirdEta = readThirdConf.params.get(0);// params sorted by name
    ConfParam readThirdIota = readThirdConf.params.get(1);
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

    ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
    def.tableName = nameOfThisMethod + "_" + RND.str(8);

    createTable(connectionGet, def);
    clearTable(connectionGet, def.tableName);

    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");

    ConfTunnelJdbc confTunnelJdbc = ConfTunnelJdbcBuilder.build(connectionGet, def);

    //
    //
    Long initialLastModified = confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    Thread.sleep(1200);
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
}
