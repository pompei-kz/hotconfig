package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import kz.pompei.hotconfig.jdbc.tst_utils.JdbcTestDbUtils;
import lombok.NonNull;
import org.testng.annotations.Test;
import utils.RND;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf\nline 2\nline 3");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is test0\nsecond line\nanother line");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param1", "value1", "This is test1\nsecond line\nanother line");

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    Conf conf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    //
    //

    assertThat(conf).isNotNull();

    assertThat(conf.confComments).isEqualTo(List.of("This is comment for conf", "line 2", "line 3"));

    ConfParam param0 = conf.params.get(0);
    ConfParam param1 = conf.params.get(1);

    assertThat(param0.name).isEqualTo("param0");
    assertThat(param1.name).isEqualTo("param1");

    assertThat(param0.valueStr).isEqualTo("value0");
    assertThat(param1.valueStr).isEqualTo("value1");

    assertThat(param0.comments).isEqualTo(List.of("This is test0", "second line", "another line"));
    assertThat(param1.comments).isEqualTo(List.of("This is test1", "second line", "another line"));
  }

  @Test(dataProvider = "databaseType")
  public void read_ordersParamsByOrderIndexNotByName(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_ordersByOrderIndex";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "config comment", 0);
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "alpha", "alpha-value", null, 3);
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "zeta", "zeta-value", null, 1);
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "middle", "middle-value", null, 2);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    Conf conf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    //
    //

    assertThat(conf).isNotNull();
    assertThat(conf.params).extracting(param -> param.name).containsExactly("zeta", "middle", "alpha");
    assertThat(conf.params).extracting(param -> param.valueStr).containsExactly("zeta-value", "middle-value", "alpha-value");
  }

  @Test(dataProvider = "databaseType")
  public void read_existsInDbTable_withThreeConfigsAndMultilineComments(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_exists_three_cfgs";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "first-config.hotconf",
      "",
      "",
      "first config comment 1\nfirst config comment 2\nfirst config comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "first-config.hotconf",
      "alpha",
      "alpha-value",
      "alpha comment 1\nalpha comment 2\nalpha comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "first-config.hotconf",
      "beta",
      "beta-value",
      "beta comment 1\nbeta comment 2\nbeta comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "first-config.hotconf",
      "gamma",
      "gamma-value",
      "gamma comment 1\ngamma comment 2\ngamma comment 3"
    );

    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "second-config.hotconf",
      "",
      "",
      "second config comment 1\nsecond config comment 2\nsecond config comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "second-config.hotconf",
      "delta",
      "delta-value",
      "delta comment 1\ndelta comment 2\ndelta comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "second-config.hotconf",
      "epsilon",
      "epsilon-value",
      "epsilon comment 1\nepsilon comment 2\nepsilon comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "second-config.hotconf",
      "zeta",
      "zeta-value",
      "zeta comment 1\nzeta comment 2\nzeta comment 3"
    );

    insertRow(
      connectionGet,
      builder,
      "other/folder",
      "third-config.hotconf",
      "",
      "",
      "third config comment 1\nthird config comment 2\nthird config comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "other/folder",
      "third-config.hotconf",
      "eta",
      "eta-value",
      "eta comment 1\neta comment 2\neta comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "other/folder",
      "third-config.hotconf",
      "theta",
      "theta-value",
      "theta comment 1\ntheta comment 2\ntheta comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "other/folder",
      "third-config.hotconf",
      "iota",
      "iota-value",
      "iota comment 1\niota comment 2\niota comment 3"
    );

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

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
    ConfParam firstAlpha = firstConf.params.get(0);
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
    ConfParam secondDelta   = secondConf.params.get(0);
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
    ConfParam thirdEta   = thirdConf.params.get(0);
    ConfParam thirdTheta = thirdConf.params.get(1);
    ConfParam thirdIota  = thirdConf.params.get(2);
    assertThat(thirdEta.name).isEqualTo("eta");
    assertThat(thirdTheta.name).isEqualTo("theta");
    assertThat(thirdIota.name).isEqualTo("iota");
    assertThat(thirdEta.valueStr).isEqualTo("eta-value");
    assertThat(thirdTheta.valueStr).isEqualTo("theta-value");
    assertThat(thirdIota.valueStr).isEqualTo("iota-value");
    assertThat(thirdEta.comments).isEqualTo(List.of("eta comment 1", "eta comment 2", "eta comment 3"));
    assertThat(thirdTheta.comments).isEqualTo(List.of("theta comment 1", "theta comment 2", "theta comment 3"));
    assertThat(thirdIota.comments).isEqualTo(List.of("iota comment 1", "iota comment 2", "iota comment 3"));
  }

  @Test(dataProvider = "databaseType")
  public void read_tableDoesNotExists(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "read_tableDoesNotExists";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    assertThat(tableExists(connectionGet, builder.tableName())).isFalse();

    //
    //
    Conf conf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    //
    //

    assertThat(conf).isNull();
    assertThat(tableExists(connectionGet, builder.tableName())).isFalse();
  }

  @Test(dataProvider = "databaseType")
  public void write_tableDoesNotExists(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "write_tableDoesNotExists";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    Conf conf = new Conf();
    conf.confComments.add("This is comment for conf");
    conf.confComments.add("line 2");
    conf.confComments.add("line 3");

    ConfParam param1 = new ConfParam();
    param1.comments.add("This is test1");
    param1.comments.add("second line");
    param1.comments.add("another line");
    param1.name     = "param1";
    param1.valueStr = "value1";
    conf.params.add(param1);

    ConfParam param0 = new ConfParam();
    param0.comments.add("This is test0");
    param0.comments.add("second line");
    param0.comments.add("another line");
    param0.name     = "param0";
    param0.valueStr = "value0";
    conf.params.add(param0);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);
    //
    //

    Conf readConf = confTunnelJdbc.read("some/folder/test-config.hotconf");

    assertThat(readConf).isNotNull();
    assertThat(readConf.confComments).isEqualTo(List.of("This is comment for conf", "line 2", "line 3"));

    ConfParam readParam0 = readConf.params.get(0);
    ConfParam readParam1 = readConf.params.get(1);

    assertThat(readParam0.name).isEqualTo("param1");
    assertThat(readParam1.name).isEqualTo("param0");

    assertThat(readParam0.valueStr).isEqualTo("value1");
    assertThat(readParam1.valueStr).isEqualTo("value0");

    assertThat(readParam0.comments).isEqualTo(List.of("This is test1", "second line", "another line"));
    assertThat(readParam1.comments).isEqualTo(List.of("This is test0", "second line", "another line"));
  }

  @Test(dataProvider = "databaseType")
  public void write_savesOrderIndexAndReadPreservesParamOrder(@NonNull DatabaseType databaseType) throws SQLException {

    String nameOfThisMethod = "write_savesOrderIndex";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    Conf conf = new Conf();

    ConfParam zeta = new ConfParam();
    zeta.name     = "zeta";
    zeta.valueStr = "zeta-value";
    conf.params.add(zeta);

    ConfParam alpha = new ConfParam();
    alpha.name     = "alpha";
    alpha.valueStr = "alpha-value";
    conf.params.add(alpha);

    ConfParam middle = new ConfParam();
    middle.name     = "middle";
    middle.valueStr = "middle-value";
    conf.params.add(middle);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);
    //
    //

    Conf readConf = confTunnelJdbc.read("some/folder/test-config.hotconf");

    assertThat(readConf).isNotNull();
    assertThat(readConf.params).extracting(param -> param.name).containsExactly("zeta", "alpha", "middle");

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colParamName}, {colOrder}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        ORDER BY {colOrder}
        """
        .replace("{colParamName}", builder.colParamName())
        .replace("{colOrder}", builder.colOrder())
        .replace("{tableName}", builder.tableName())
        .replace("{colFolder}", builder.colFolder())
        .replace("{colConfigName}", builder.colConfigName());

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, "some/folder");
        ps.setString(2, "test-config.hotconf");
        try (ResultSet rs = ps.executeQuery()) {
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("");
          assertThat(rs.getInt(builder.colOrder())).isEqualTo(0);
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("zeta");
          assertThat(rs.getInt(builder.colOrder())).isEqualTo(1);
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("alpha");
          assertThat(rs.getInt(builder.colOrder())).isEqualTo(2);
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("middle");
          assertThat(rs.getInt(builder.colOrder())).isEqualTo(3);
          assertThat(rs.next()).isFalse();
        }
      }
    }
  }

  @Test(dataProvider = "databaseType")
  public void writeAndRead_differentTableNamesAreIsolated(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "differentTableNames";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder firstBuilder = ConfigTunnelJdbc.builder();
    firstBuilder.tableName(nameOfThisMethod + "_first_" + RND.str(8));

    ConfigTunnelJdbcBuilder secondBuilder = ConfigTunnelJdbc.builder();
    secondBuilder.tableName(nameOfThisMethod + "_second_" + RND.str(8));

    ConfigTunnelJdbc firstTunnel  = firstBuilder.connectionGet(connectionGet).build();
    ConfigTunnelJdbc secondTunnel = secondBuilder.connectionGet(connectionGet).build();

    Conf firstConf = new Conf();
    firstConf.confComments.add("first table config");
    ConfParam firstParam = new ConfParam();
    firstParam.name     = "param";
    firstParam.valueStr = "first-value";
    firstConf.params.add(firstParam);

    Conf secondConf = new Conf();
    secondConf.confComments.add("second table config");
    ConfParam secondParam = new ConfParam();
    secondParam.name     = "param";
    secondParam.valueStr = "second-value";
    secondConf.params.add(secondParam);

    String localPath = "same/folder/test-config.hotconf";

    firstTunnel.write(localPath, firstConf);
    secondTunnel.write(localPath, secondConf);

    Conf readFirstConf  = firstTunnel.read(localPath);
    Conf readSecondConf = secondTunnel.read(localPath);

    assertThat(tableExists(connectionGet, firstBuilder.tableName())).isTrue();
    assertThat(tableExists(connectionGet, secondBuilder.tableName())).isTrue();

    assertThat(readFirstConf).isNotNull();
    assertThat(readSecondConf).isNotNull();
    assertThat(readFirstConf.confComments).isEqualTo(List.of("first table config"));
    assertThat(readSecondConf.confComments).isEqualTo(List.of("second table config"));
    assertThat(readFirstConf.params.get(0).valueStr).isEqualTo("first-value");
    assertThat(readSecondConf.params.get(0).valueStr).isEqualTo("second-value");
  }

  @Test(dataProvider = "databaseType")
  public void writeAndRead_customTableAndColumnNames(@NonNull DatabaseType databaseType) throws SQLException {

    String nameOfThisMethod = "customTableAndColumns";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder()
                                                      .tableName(nameOfThisMethod + "_" + RND.str(8))
                                                      .colFolder("cfg_folder")
                                                      .colConfigName("cfg_name")
                                                      .colParamName("cfg_param")
                                                      .colParamValueStr("cfg_value")
                                                      .colComment("cfg_comment")
                                                      .colError("cfg_error")
                                                      .colNotice("cfg_notice")
                                                      .colOrder("cfg_order")
                                                      .colCreatedAt("cfg_created")
                                                      .colLastModified("cfg_modified");

    Conf conf = new Conf();
    conf.confComments.add("custom columns config comment");

    ConfParam param = new ConfParam();
    param.name     = "custom_param";
    param.valueStr = "custom value";
    param.error    = "custom error";
    param.comments.add("custom param comment");
    conf.params.add(param);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();
    confTunnelJdbc.write("custom/folder/custom-config.hotconf", conf);
    confTunnelJdbc.writeNoticeLines("custom/folder/custom-config.hotconf", List.of("custom notice"));

    Conf readConf = confTunnelJdbc.read("custom/folder/custom-config.hotconf");

    assertThat(readConf).isNotNull();
    assertThat(readConf.confComments).isEqualTo(List.of("custom columns config comment"));
    assertThat(readConf.params).hasSize(1);
    assertThat(readConf.params.get(0).name).isEqualTo("custom_param");
    assertThat(readConf.params.get(0).valueStr).isEqualTo("custom value");
    assertThat(readConf.params.get(0).error).isEqualTo("custom error");
    assertThat(readConf.params.get(0).comments).isEqualTo(List.of("custom param comment"));
    assertThat(confTunnelJdbc.readNoticeLines("custom/folder/custom-config.hotconf")).isEqualTo(List.of("custom notice"));

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colParamName}, {colParamValueStr}, {colComment}, {colError}, {colNotice}, {colOrder}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        ORDER BY {colOrder}, {colParamName}
        """
        .replace("{colParamName}", builder.colParamName())
        .replace("{colParamValueStr}", builder.colParamValueStr())
        .replace("{colComment}", builder.colComment())
        .replace("{colError}", builder.colError())
        .replace("{colNotice}", builder.colNotice())
        .replace("{colOrder}", builder.colOrder())
        .replace("{tableName}", builder.tableName())
        .replace("{colFolder}", builder.colFolder())
        .replace("{colConfigName}", builder.colConfigName());

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, "custom/folder");
        ps.setString(2, "custom-config.hotconf");
        try (ResultSet rs = ps.executeQuery()) {
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("");
          assertThat(rs.getInt(builder.colOrder())).isEqualTo(0);
          assertThat(rs.getString(builder.colComment())).isEqualTo("custom columns config comment");
          assertThat(rs.getString(builder.colNotice())).isEqualTo("custom notice");
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("custom_param");
          assertThat(rs.getInt(builder.colOrder())).isEqualTo(1);
          assertThat(rs.getString(builder.colParamValueStr())).isEqualTo("custom value");
          assertThat(rs.getString(builder.colComment())).isEqualTo("custom param comment");
          assertThat(rs.getString(builder.colError())).isEqualTo("custom error");
          assertThat(rs.getString(builder.colNotice())).isNull();
          assertThat(rs.next()).isFalse();
        }
      }
    }
  }

  @Test(dataProvider = "databaseType")
  public void createTable_usesEveryConfiguredColumnName(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "createTable_allCustomColumns";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder()
                                                      .tableName(nameOfThisMethod + "_" + RND.str(8))
                                                      .colComment("comment_text_custom")
                                                      .colConfigName("config_name_custom")
                                                      .colCreatedAt("created_at_custom")
                                                      .colError("error_text_custom")
                                                      .colFolder("folder_path_custom")
                                                      .colLastModified("modified_at_custom")
                                                      .colNotice("notice_text_custom")
                                                      .colOrder("order_index_custom")
                                                      .colParamName("param_name_custom")
                                                      .colParamValueStr("param_value_custom");

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    confTunnelJdbc.createTableIfNotExists();
    //
    //

    assertThat(tableExists(connectionGet, builder.tableName())).isTrue();

    assertThat(columnExists(connectionGet, builder.tableName(), builder.colComment())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colConfigName())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colCreatedAt())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colError())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colFolder())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colLastModified())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colNotice())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colOrder())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colParamName())).isTrue();
    assertThat(columnExists(connectionGet, builder.tableName(), builder.colParamValueStr())).isTrue();

    assertThat(columnExists(connectionGet, builder.tableName(), "cmt")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "class_name")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "created_at")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "error")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "folder")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "last_modified_at")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "notice")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "order_index")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "name")).isFalse();
    assertThat(columnExists(connectionGet, builder.tableName(), "value_str")).isFalse();
  }

  @Test(dataProvider = "databaseType")
  public void writeAndRead_paramError(@NonNull DatabaseType databaseType) throws SQLException {

    String nameOfThisMethod = "writeAndRead_paramError";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    Conf conf = new Conf();

    ConfParam param0 = new ConfParam();
    param0.name     = "param0";
    param0.valueStr = "value0";
    param0.error    = "param0 parse failed";
    conf.params.add(param0);

    ConfParam param1 = new ConfParam();
    param1.name     = "param1";
    param1.valueStr = "value1";
    conf.params.add(param1);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);
    //
    //

    Conf readConf = confTunnelJdbc.read("some/folder/test-config.hotconf");

    assertThat(readConf).isNotNull();
    assertThat(readConf.params).hasSize(2);

    ConfParam readParam0 = readConf.params.get(0);
    ConfParam readParam1 = readConf.params.get(1);

    assertThat(readParam0.name).isEqualTo("param0");
    assertThat(readParam0.error).isEqualTo("param0 parse failed");
    assertThat(readParam1.name).isEqualTo("param1");
    assertThat(readParam1.error).isNull();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colParamName}, {colError}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        ORDER BY {colOrder}, {colParamName}
        """
        .replace("{colParamName}", builder.colParamName())
        .replace("{colError}", builder.colError())
        .replace("{colOrder}", builder.colOrder())
        .replace("{tableName}", builder.tableName())
        .replace("{colFolder}", builder.colFolder())
        .replace("{colConfigName}", builder.colConfigName());

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, "some/folder");
        ps.setString(2, "test-config.hotconf");
        try (ResultSet rs = ps.executeQuery()) {
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("");
          assertThat(rs.getString(builder.colError())).isNull();
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("param0");
          assertThat(rs.getString(builder.colError())).isEqualTo("param0 parse failed");
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("param1");
          assertThat(rs.getString(builder.colError())).isNull();
          assertThat(rs.next()).isFalse();
        }
      }
    }
  }

  @Test(dataProvider = "databaseType")
  public void write_savesTwoConfigsWithMultilineComments(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "write_savesTwoConfigsWithMultilineComments";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

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

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

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

    ConfParam readFirstAlpha = readFirstConf.params.get(0);
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

    ConfParam readSecondDelta   = readSecondConf.params.get(0);
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

    ConfParam readThirdEta   = readThirdConf.params.get(0);
    ConfParam readThirdTheta = readThirdConf.params.get(1);
    ConfParam readThirdIota  = readThirdConf.params.get(2);

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

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    Long initialLastModified = confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    waitForChange(databaseType);
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "changed", "This is comment for conf");

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

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(
      connectionGet,
      builder,
      "some/folder",
      "first-config.hotconf",
      "alpha",
      "alpha-value",
      "alpha comment 1\nalpha comment 2\nalpha comment 3"
    );
    insertRow(
      connectionGet,
      builder,
      "other/folder",
      "second-config.hotconf",
      "beta",
      "beta-value",
      "beta comment 1\nbeta comment 2\nbeta comment 3"
    );

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    Long initialFirstMarker  = confTunnelJdbc.modificationMarker("some/folder/first-config.hotconf");
    Long initialSecondMarker = confTunnelJdbc.modificationMarker("other/folder/second-config.hotconf");
    //
    //

    waitForChange(databaseType);
    updateRow(
      connectionGet,
      builder,
      "other/folder",
      "second-config.hotconf",
      "beta",
      "beta-value-changed",
      "beta comment 1\nbeta comment 2\nbeta comment 3"
    );
    updateRow(
      connectionGet,
      builder,
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

  @Test(dataProvider = "databaseType")
  public void readNoticeLines_missingConfig(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "readNoticeLines_missingConfig";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    //
    //
    List<String> lines = confTunnelJdbc.readNoticeLines("some/folder/test-config.hotconf");
    //
    //

    assertThat(lines).isEmpty();
    assertThat(tableExists(connectionGet, builder.tableName())).isFalse();
  }

  @Test(dataProvider = "databaseType")
  public void writeNoticeLines_configRowOnly(@NonNull DatabaseType databaseType) throws SQLException {

    String nameOfThisMethod = "writeNoticeLines_configRowOnly";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    Conf conf = new Conf();
    ConfParam param = new ConfParam();
    param.name     = "param0";
    param.valueStr = "value0";
    conf.params.add(param);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);

    //
    //
    confTunnelJdbc.writeNoticeLines("some/folder/test-config.hotconf", List.of("notice line 1", "notice line 2", ""));
    //
    //

    assertThat(confTunnelJdbc.readNoticeLines("some/folder/test-config.hotconf"))
      .isEqualTo(List.of("notice line 1", "notice line 2", ""));

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colParamName}, {colNotice}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        ORDER BY {colOrder}, {colParamName}
        """
        .replace("{colParamName}", builder.colParamName())
        .replace("{colNotice}", builder.colNotice())
        .replace("{colOrder}", builder.colOrder())
        .replace("{tableName}", builder.tableName())
        .replace("{colFolder}", builder.colFolder())
        .replace("{colConfigName}", builder.colConfigName());

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, "some/folder");
        ps.setString(2, "test-config.hotconf");
        try (ResultSet rs = ps.executeQuery()) {
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("");
          assertThat(rs.getString(builder.colNotice())).isEqualTo("notice line 1\nnotice line 2\n");
          assertThat(rs.next()).isTrue();
          assertThat(rs.getString(builder.colParamName())).isEqualTo("param0");
          assertThat(rs.getString(builder.colNotice())).isNull();
          assertThat(rs.next()).isFalse();
        }
      }
    }
  }

  @Test(dataProvider = "databaseType")
  public void trigger_updatesLastModifiedOfChangedRow(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "trigger_changedRow";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    Long paramLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0");

    waitForChange(databaseType);

    //
    //
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");
    //
    //

    Long paramLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0");

    assertThat(paramLastModifiedBefore).isNotNull();
    assertThat(paramLastModifiedAfter).isNotNull();
    assertThat(paramLastModifiedAfter).isGreaterThan(paramLastModifiedBefore);
  }

  @Test(dataProvider = "databaseType")
  public void trigger_updatesLastModifiedOfConfigRow_whenParamChanged(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "trigger_configRow";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    Long confLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    waitForChange(databaseType);

    //
    //
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");
    //
    //

    Long confLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    assertThat(confLastModifiedBefore).isNotNull();
    assertThat(confLastModifiedAfter).isNotNull();

    if (databaseType == DatabaseType.MariaDB) {
      // MariaDB forbids a trigger to change the table it is attached to (error 1442),
      // so the row of the whole config cannot be touched from the trigger
      assertThat(confLastModifiedAfter).isEqualTo(confLastModifiedBefore);
      return;
    }

    assertThat(confLastModifiedAfter).isGreaterThan(confLastModifiedBefore);
  }

  @Test(dataProvider = "databaseType")
  public void trigger_keepsConfigRowOfOtherConfigsUntouched(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "trigger_otherConfigs";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "first-config.hotconf", "", "", "first conf comment");
    insertRow(connectionGet, builder, "some/folder", "first-config.hotconf", "param0", "value0", "first param comment");
    insertRow(connectionGet, builder, "some/folder", "second-config.hotconf", "", "", "second conf comment");
    insertRow(connectionGet, builder, "some/folder", "second-config.hotconf", "param0", "value0", "second param comment");

    Long secondConfLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "second-config.hotconf", "");

    waitForChange(databaseType);

    //
    //
    updateRow(connectionGet, builder, "some/folder", "first-config.hotconf", "param0", "value0-changed", "first param comment");
    //
    //

    Long secondConfLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "second-config.hotconf", "");

    assertThat(secondConfLastModifiedBefore).isNotNull();
    assertThat(secondConfLastModifiedAfter).isNotNull();
    assertThat(secondConfLastModifiedAfter).isEqualTo(secondConfLastModifiedBefore);
  }

  @Test(dataProvider = "databaseType")
  public void trigger_customTriggerNames(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "trigger_customNames";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));
    builder.triggerName("my_own_%_trg");
    builder.triggerFnName("my_own_%_trg_fn");

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    Long confLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    waitForChange(databaseType);

    //
    //
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");
    //
    //

    Long confLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    assertThat(triggerExists(connectionGet, builder.tableName(), "my_own_" + builder.tableName() + "_trg")).isTrue();

    assertThat(confLastModifiedBefore).isNotNull();
    assertThat(confLastModifiedAfter).isNotNull();

    if (databaseType == DatabaseType.MariaDB) {
      assertThat(confLastModifiedAfter).isEqualTo(confLastModifiedBefore);
      return;
    }

    assertThat(confLastModifiedAfter).isGreaterThan(confLastModifiedBefore);
  }

  @Test
  public void build_emptyTriggerName() {

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.connectionGet(createConnectionGet(DatabaseType.PostgreSQL, "build_emptyTriggerName"));
    builder.triggerName("");

    assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void build_triggerNameWithSchema() {

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.connectionGet(createConnectionGet(DatabaseType.PostgreSQL, "build_triggerNameWithSchema"));
    builder.triggerFnName("some_schema.%_trg_fn");

    assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class);
  }

  /**
   * In MariaDB a schema is a separate database, and the test user has no rights to create one,
   * so a schema-qualified table name is checked in PostgreSQL only.
   */
  @Test
  public void trigger_tableInSchema() throws InterruptedException {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "trigger_tableInSchema";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    String simpleTableName = nameOfThisMethod + "_" + RND.str(8);
    builder.tableName("trigger_schema." + simpleTableName);

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    Long confLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    waitForChange(databaseType);

    //
    //
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");
    //
    //

    Long confLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    assertThat(triggerExists(connectionGet, simpleTableName, simpleTableName + "__trigger")).isTrue();

    assertThat(confLastModifiedBefore).isNotNull();
    assertThat(confLastModifiedAfter).isNotNull();
    assertThat(confLastModifiedAfter).isGreaterThan(confLastModifiedBefore);
  }

  @Test(dataProvider = "databaseType")
  public void trigger_createdWhenTableExistsWithoutTrigger(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "trigger_tableWithoutTrigger";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    String triggerName = builder.tableName() + "__trigger";

    // the table stays, the trigger goes away - as it happens with a table created by an older version or by hand
    dropTrigger(connectionGet, databaseType, builder.tableName(), triggerName);
    assertThat(triggerExists(connectionGet, builder.tableName(), triggerName)).isFalse();

    //
    //
    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();
    confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    assertThat(triggerExists(connectionGet, builder.tableName(), triggerName)).isTrue();

    Long confLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    waitForChange(databaseType);
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");

    Long paramLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0");
    Long confLastModifiedAfter  = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    assertThat(confLastModifiedBefore).isNotNull();
    assertThat(paramLastModifiedAfter).isNotNull();
    assertThat(confLastModifiedAfter).isNotNull();
    assertThat(paramLastModifiedAfter).isGreaterThan(confLastModifiedBefore);

    if (databaseType == DatabaseType.MariaDB) {
      assertThat(confLastModifiedAfter).isEqualTo(confLastModifiedBefore);
      return;
    }

    assertThat(confLastModifiedAfter).isGreaterThan(confLastModifiedBefore);
  }

  /**
   * An existing trigger must stay the very same object: dropping and creating it again would leave a moment
   * when changes of the table are not caught by anybody.
   * <p>
   * The identity of the trigger is visible in PostgreSQL only, so this is a PostgreSQL test.
   */
  @Test
  public void trigger_notRecreatedWhenItAlreadyExists() {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "trigger_notRecreated";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);

    String triggerName = builder.tableName() + "__trigger";

    Long triggerOidBefore = triggerOidPg(connectionGet, builder.tableName(), triggerName);

    //
    //
    createTable(connectionGet, builder);
    //
    //

    Long triggerOidAfter = triggerOidPg(connectionGet, builder.tableName(), triggerName);

    assertThat(triggerOidBefore).isNotNull();
    assertThat(triggerOidAfter).isEqualTo(triggerOidBefore);
  }

  /**
   * The trigger of an older version of the library keeps the name but does something else. Such a trigger must be brought
   * up to date, otherwise a change of a parameter stays unnoticed.
   */
  @Test
  public void trigger_staleFunctionBodyIsReplaced_pg() throws InterruptedException {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "trigger_staleFunctionBody";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    // the trigger stays, but its function does nothing - as it would be after an upgrade of the library
    execSql(connectionGet, """
      CREATE OR REPLACE FUNCTION {functionName}() RETURNS trigger AS $$
      BEGIN
        RETURN NEW;
      END;
      $$ LANGUAGE plpgsql
      """.replace("{functionName}", builder.tableName() + "__trigger_fn"));

    //
    //
    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();
    confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    Long confLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    waitForChange(databaseType);
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");

    Long confLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    assertThat(confLastModifiedBefore).isNotNull();
    assertThat(confLastModifiedAfter).isNotNull();
    assertThat(confLastModifiedAfter).isGreaterThan(confLastModifiedBefore);
  }

  /**
   * The same as {@link #trigger_staleFunctionBodyIsReplaced_pg()}, but MariaDB keeps the body inside the trigger itself.
   */
  @Test
  public void trigger_staleTriggerBodyIsReplaced_mariaDb() throws InterruptedException {

    DatabaseType databaseType = DatabaseType.MariaDB;

    String nameOfThisMethod = "trigger_staleTriggerBody";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");

    String triggerName = builder.tableName() + "__trigger";

    // the trigger stays, but its body does something else - as it would be after an upgrade of the library
    execSql(connectionGet, """
      CREATE OR REPLACE TRIGGER {triggerName}
      BEFORE UPDATE ON {tableName}
      FOR EACH ROW
      SET NEW.{colComment} = 'body of an older version'
      """
      .replace("{triggerName}", triggerName)
      .replace("{tableName}", builder.tableName())
      .replace("{colComment}", builder.colComment()));

    //
    //
    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();
    confTunnelJdbc.modificationMarker("some/folder/test-config.hotconf");
    //
    //

    assertThat(triggerBody(connectionGet, builder.tableName(), triggerName))
      .isEqualTo("SET NEW." + builder.colLastModified() + " = CURRENT_TIMESTAMP");

    Long confLastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    waitForChange(databaseType);
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "changed", "This is comment for conf");

    Long confLastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "");

    assertThat(confLastModifiedBefore).isNotNull();
    assertThat(confLastModifiedAfter).isNotNull();
    assertThat(confLastModifiedAfter).isGreaterThan(confLastModifiedBefore);
  }

  /**
   * A fresh table keeps the timestamps with the time zone, so the moment of every row is known without any guessing about
   * the time zone of the one who wrote it.
   * <p>
   * MariaDB has no {@code TIMESTAMPTZ}: its own {@code TIMESTAMP} is stored in UTC and already keeps the moment itself,
   * so this is a PostgreSQL test.
   */
  @Test
  public void createTable_timestampColumnsKeepTimeZone_pg() {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "createTable_timestampTz";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    //
    //
    createTable(connectionGet, builder);
    //
    //

    assertThat(columnDataType(connectionGet, builder.tableName(), builder.colCreatedAt())).isEqualTo("timestamp with time zone");
    assertThat(columnDataType(connectionGet, builder.tableName(), builder.colLastModified())).isEqualTo("timestamp with time zone");
  }

  /**
   * The table of an older version of the library keeps the timestamps as a plain {@code TIMESTAMP} - a wall clock without any
   * time zone, which two applications in two time zones read as two different moments. Such a table must be brought to
   * {@code TIMESTAMPTZ} when the table is checked, and the values of the rows must stay where they were.
   * <p>
   * MariaDB has no {@code TIMESTAMPTZ}: its own {@code TIMESTAMP} is stored in UTC and already keeps the moment itself,
   * so this is a PostgreSQL test.
   */
  @Test
  public void timestampColumns_broughtToTimestampTz_withValuesKept_pg() {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "timestamps_toTimestampTz";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    String tableName = builder.tableName();

    // the table of an older version of the library: the timestamps have no time zone
    execSql(connectionGet, "ALTER TABLE " + tableName
                             + " ALTER COLUMN " + builder.colCreatedAt() + " TYPE TIMESTAMP"
                             + ", ALTER COLUMN " + builder.colLastModified() + " TYPE TIMESTAMP");

    assertThat(columnDataType(connectionGet, tableName, builder.colCreatedAt())).isEqualTo("timestamp without time zone");
    assertThat(columnDataType(connectionGet, tableName, builder.colLastModified())).isEqualTo("timestamp without time zone");

    String createdAt    = "2020-01-02 03:04:05.123456";
    String lastModified = "2021-02-03 04:05:06.654321";

    // the row of the old table, with the timestamps of the old type; the trigger of the table changes the last modification
    // timestamp of a row which is updated, so the values are given to the row at once, by the INSERT itself
    execSql(connectionGet, "INSERT INTO " + tableName
                             + " (" + builder.colFolder()
                             + ", " + builder.colConfigName()
                             + ", " + builder.colParamName()
                             + ", " + builder.colParamValueStr()
                             + ", " + builder.colOrder()
                             + ", " + builder.colCreatedAt()
                             + ", " + builder.colLastModified() + ")"
                             + " VALUES ('some/folder', 'test-config.hotconf', 'param0', 'value0', 1"
                             + ", '" + createdAt + "', '" + lastModified + "')");

    Long lastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0");

    //
    //
    newTunnel(connectionGet, tableName).createTableIfNotExists();
    //
    //

    assertThat(columnDataType(connectionGet, tableName, builder.colCreatedAt())).isEqualTo("timestamp with time zone");
    assertThat(columnDataType(connectionGet, tableName, builder.colLastModified())).isEqualTo("timestamp with time zone");

    // the values of the columns stay where they were - the very same wall clock in the very same time zone
    assertThat(timestampAsText(connectionGet, builder, builder.colCreatedAt(), "some/folder", "test-config.hotconf", "param0"))
      .isEqualTo(createdAt);
    assertThat(timestampAsText(connectionGet, builder, builder.colLastModified(), "some/folder", "test-config.hotconf", "param0"))
      .isEqualTo(lastModified);

    // and the tunnel reads the very same moment as before the change
    assertThat(lastModifiedBefore).isNotNull();
    assertThat(lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0")).isEqualTo(lastModifiedBefore);

    // the second run has nothing left to do, and it breaks nothing
    newTunnel(connectionGet, tableName).createTableIfNotExists();

    assertThat(columnDataType(connectionGet, tableName, builder.colLastModified())).isEqualTo("timestamp with time zone");
    assertThat(timestampAsText(connectionGet, builder, builder.colLastModified(), "some/folder", "test-config.hotconf", "param0"))
      .isEqualTo(lastModified);
  }

  /**
   * The old table lives in its own schema. The check of the type must find such a column too, otherwise the timestamps of a
   * table with a schema would stay without a time zone forever.
   */
  @Test
  public void timestampColumns_broughtToTimestampTz_tableInSchema_pg() {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "timestamps_toTimestampTz_schema";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    String simpleTableName = nameOfThisMethod + "_" + RND.str(8);
    String schemaName      = "schema_" + RND.str(8);
    String tableName       = schemaName + "." + simpleTableName;

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(tableName);

    createTable(connectionGet, builder);

    execSql(connectionGet, "ALTER TABLE " + tableName
                             + " ALTER COLUMN " + builder.colCreatedAt() + " TYPE TIMESTAMP"
                             + ", ALTER COLUMN " + builder.colLastModified() + " TYPE TIMESTAMP");

    assertThat(columnDataType(connectionGet, simpleTableName, builder.colLastModified())).isEqualTo("timestamp without time zone");

    //
    //
    newTunnel(connectionGet, tableName).createTableIfNotExists();
    //
    //

    assertThat(columnDataType(connectionGet, simpleTableName, builder.colCreatedAt())).isEqualTo("timestamp with time zone");
    assertThat(columnDataType(connectionGet, simpleTableName, builder.colLastModified())).isEqualTo("timestamp with time zone");

    execSql(connectionGet, "DROP SCHEMA " + schemaName + " CASCADE");
  }

  /**
   * The timestamps of the table have no time zone yet and several instances start at once: now they race for
   * {@code ALTER COLUMN ... TYPE TIMESTAMPTZ}. Nobody may fall on this.
   */
  @Test
  public void alterTimestampColumns_raceOfManyInstances_pg() throws InterruptedException {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "timestamps_toTimestampTz_race";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);

    String tableName = builder.tableName();

    execSql(connectionGet, "ALTER TABLE " + tableName
                             + " ALTER COLUMN " + builder.colCreatedAt() + " TYPE TIMESTAMP"
                             + ", ALTER COLUMN " + builder.colLastModified() + " TYPE TIMESTAMP");

    //
    //
    List<Throwable> errors = runAtTheSameMoment(INSTANCE_COUNT, () -> newTunnel(connectionGet, tableName).createTableIfNotExists());
    //
    //

    assertThat(describeAll(errors)).isEmpty();
    assertThat(columnDataType(connectionGet, tableName, builder.colCreatedAt())).isEqualTo("timestamp with time zone");
    assertThat(columnDataType(connectionGet, tableName, builder.colLastModified())).isEqualTo("timestamp with time zone");
  }

  /**
   * Two instances of the application start at the same moment and both create the table of the config. Nobody may fall on this.
   */
  @Test(dataProvider = "databaseType")
  public void createTable_raceOfManyInstances(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "createTable_race";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    String tableName = nameOfThisMethod + "_" + RND.str(8);

    //
    //
    List<Throwable> errors = runAtTheSameMoment(INSTANCE_COUNT, () -> newTunnel(connectionGet, tableName).createTableIfNotExists());
    //
    //

    assertThat(describeAll(errors)).isEmpty();

    // the table of the race is a working table of the config, not a half-made one
    assertThat(tableExists(connectionGet, tableName)).isTrue();
    assertThat(triggerExists(connectionGet, tableName, tableName + "__trigger")).isTrue();

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(tableName);

    Conf      conf  = new Conf();
    ConfParam param = new ConfParam();
    param.name     = "param0";
    param.valueStr = "value0";
    conf.params.add(param);

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);

    Conf readConf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    assertThat(readConf).isNotNull();
    assertThat(readConf.params.get(0).valueStr).isEqualTo("value0");
  }

  /**
   * The table is already there without a trigger, and several instances start at once: now they race for the trigger and,
   * in PostgreSQL, for the function of the trigger.
   */
  @Test(dataProvider = "databaseType")
  public void createTrigger_raceOfManyInstances(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "createTrigger_race";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);

    String tableName   = builder.tableName();
    String triggerName = tableName + "__trigger";

    dropTrigger(connectionGet, databaseType, tableName, triggerName);
    if (databaseType == DatabaseType.PostgreSQL) {
      execSql(connectionGet, "DROP FUNCTION IF EXISTS " + tableName + "__trigger_fn()");
    }
    assertThat(triggerExists(connectionGet, tableName, triggerName)).isFalse();

    //
    //
    List<Throwable> errors = runAtTheSameMoment(INSTANCE_COUNT, () -> newTunnel(connectionGet, tableName).createTableIfNotExists());
    //
    //

    assertThat(describeAll(errors)).isEmpty();
    assertThat(triggerExists(connectionGet, tableName, triggerName)).isTrue();
  }

  /**
   * The table is already there from an older version, without the column of the row order, and several instances start at once:
   * now they race for {@code ALTER TABLE ... ADD COLUMN}.
   */
  @Test(dataProvider = "databaseType")
  public void addOrderColumn_raceOfManyInstances(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "addOrderColumn_race";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));

    createTable(connectionGet, builder);

    String tableName = builder.tableName();

    execSql(connectionGet, "ALTER TABLE " + tableName + " DROP COLUMN " + builder.colOrder());
    assertThat(columnExists(connectionGet, tableName, builder.colOrder())).isFalse();

    //
    //
    List<Throwable> errors = runAtTheSameMoment(INSTANCE_COUNT, () -> newTunnel(connectionGet, tableName).createTableIfNotExists());
    //
    //

    assertThat(describeAll(errors)).isEmpty();
    assertThat(columnExists(connectionGet, tableName, builder.colOrder())).isTrue();
  }

  /**
   * The schema of the table does not exist yet and several instances start at once: now they race for {@code CREATE SCHEMA}.
   * <p>
   * In MariaDB a schema is a separate database, and the test user has no rights to create one, so this is a PostgreSQL test.
   */
  @Test
  public void createSchema_raceOfManyInstances() throws InterruptedException {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "createSchema_race";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    String simpleTableName = nameOfThisMethod + "_" + RND.str(8);
    String schemaName      = "schema_" + RND.str(8);
    String tableName       = schemaName + "." + simpleTableName;

    //
    //
    List<Throwable> errors = runAtTheSameMoment(INSTANCE_COUNT, () -> newTunnel(connectionGet, tableName).createTableIfNotExists());
    //
    //

    assertThat(describeAll(errors)).isEmpty();
    assertThat(tableExists(connectionGet, simpleTableName)).isTrue();
    assertThat(triggerExists(connectionGet, simpleTableName, simpleTableName + "__trigger")).isTrue();

    execSql(connectionGet, "DROP SCHEMA " + schemaName + " CASCADE");
  }

  /**
   * The loser of the race sees no table, tries to create it and gets the table of the winner. This is exactly the state of a
   * tunnel whose check of the table says "no table" while the table is already there.
   */
  @Test(dataProvider = "databaseType")
  public void createTable_lostRaceIsNotAFailure(@NonNull DatabaseType databaseType) {

    String nameOfThisMethod = "createTable_lostRace";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));
    builder.connectionGet(connectionGet);

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    ConfigTunnelJdbc loserOfTheRace = tunnelWhichSeesNoTable(databaseType, connectionGet, builder.buildDef());

    //
    //
    loserOfTheRace.createTableIfNotExists();
    //
    //

    // the table of the winner is left as it is, with all its data
    Conf conf = loserOfTheRace.read("some/folder/test-config.hotconf");
    assertThat(conf).isNotNull();
    assertThat(conf.params).hasSize(1);
    assertThat(conf.params.get(0).valueStr).isEqualTo("value0");
  }

  /**
   * The error of a lost race is swallowed, and only it: an error of any other kind must go up as it is.
   */
  @Test(dataProvider = "databaseType")
  public void isDuplicateObject_onlyForRaceErrors(@NonNull DatabaseType databaseType) {

    ConnectionGet connectionGet = createConnectionGet(databaseType, "isDuplicateObject");

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName("isDuplicateObject_" + RND.str(8));

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    // PostgreSQL: duplicate_table, duplicate_schema, duplicate_column, duplicate_object, duplicate_function
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("relation \"conf\" already exists", "42P07"))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("schema \"conf\" already exists", "42P06"))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("column \"order_index\" already exists", "42701"))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("trigger \"conf__trigger\" already exists", "42710"))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("function \"conf__trigger_fn\" already exists", "42723"))).isTrue();

    // PostgreSQL shows the collision of two CREATE statements as a unique violation inside its own catalog
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException(
      "ERROR: duplicate key value violates unique constraint \"pg_type_typname_nsp_index\"", "23505"))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException(
      "ERROR: duplicate key value violates unique constraint \"pg_class_relname_nsp_index\"", "23505"))).isTrue();

    // MariaDB: table exists, database exists, column exists, trigger exists
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("Table 'conf' already exists", "42S01", 1050))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("Can't create database 'conf'", "HY000", 1007))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("Duplicate column name 'order_index'", "42S21", 1060))).isTrue();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("Trigger already exists", "HY000", 1359))).isTrue();

    // the same error hidden inside the chain of errors
    SQLException chain = new SQLException("Could not create table", "HY000");
    chain.setNextException(new SQLException("relation \"conf\" already exists", "42P07"));
    assertThat(confTunnelJdbc.isDuplicateObject(chain)).isTrue();

    // a unique violation on the data of the application is not a race of two CREATE statements
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException(
      "ERROR: duplicate key value violates unique constraint \"conf_pkey\"", "23505"))).isFalse();

    // and neither is anything else
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("permission denied for schema public", "42501"))).isFalse();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("relation \"conf\" does not exist", "42P01"))).isFalse();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("connection failure", "08006"))).isFalse();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("Access denied for user", "28000", 1045))).isFalse();
    assertThat(confTunnelJdbc.isDuplicateObject(new SQLException("something went wrong"))).isFalse();
  }

  /**
   * The loser of the race sees no trigger, tries to create it and gets the trigger of the winner. In PostgreSQL the function of
   * the trigger is created the same way, so this covers both.
   */
  @Test(dataProvider = "databaseType")
  public void createTrigger_lostRaceIsNotAFailure(@NonNull DatabaseType databaseType) throws InterruptedException {

    String nameOfThisMethod = "createTrigger_lostRace";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(nameOfThisMethod + "_" + RND.str(8));
    builder.connectionGet(connectionGet);

    createTable(connectionGet, builder);
    clearTable(connectionGet, builder.tableName());

    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "", "", "This is comment for conf");
    insertRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0", "This is comment for param0");

    ConfigTunnelJdbc loserOfTheRace = tunnelWhichSeesNothing(databaseType, connectionGet, builder.buildDef());

    //
    //
    loserOfTheRace.createTableIfNotExists();
    //
    //

    // the trigger of the winner is left in place and works
    assertThat(triggerExists(connectionGet, builder.tableName(), builder.tableName() + "__trigger")).isTrue();

    Long lastModifiedBefore = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0");

    waitForChange(databaseType);
    updateRow(connectionGet, builder, "some/folder", "test-config.hotconf", "param0", "value0-changed", "This is comment for param0");

    Long lastModifiedAfter = lastModified(connectionGet, builder, "some/folder", "test-config.hotconf", "param0");

    assertThat(lastModifiedBefore).isNotNull();
    assertThat(lastModifiedAfter).isNotNull();
    assertThat(lastModifiedAfter).isGreaterThan(lastModifiedBefore);
  }

  /**
   * The loser of the race for the schema sees no schema, tries to create it and gets the schema of the winner. The table of the
   * config is created after the schema, so this error may not stop the work: the table must still be created.
   * <p>
   * In MariaDB a schema is a separate database, and the test user has no rights to create one, so this is a PostgreSQL test.
   */
  @Test
  public void createSchema_lostRaceIsNotAFailure() {

    DatabaseType databaseType = DatabaseType.PostgreSQL;

    String nameOfThisMethod = "createSchema_lostRace";

    ConnectionGet connectionGet = createConnectionGet(databaseType, nameOfThisMethod);

    String schemaName      = "schema_" + RND.str(8);
    String simpleTableName = nameOfThisMethod + "_" + RND.str(8);

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(schemaName + "." + simpleTableName);
    builder.connectionGet(connectionGet);

    // the winner of the race has already made the schema
    execSql(connectionGet, "CREATE SCHEMA " + schemaName);

    ConfigTunnelJdbc loserOfTheRace = tunnelWhichSeesNothing(databaseType, connectionGet, builder.buildDef());

    //
    //
    loserOfTheRace.createTableIfNotExists();
    //
    //

    // the schema of the winner is left as it is, and the table of the config is made in it
    assertThat(tableExists(connectionGet, simpleTableName)).isTrue();
    assertThat(triggerExists(connectionGet, simpleTableName, simpleTableName + "__trigger")).isTrue();

    Conf      conf  = new Conf();
    ConfParam param = new ConfParam();
    param.name     = "param0";
    param.valueStr = "value0";
    conf.params.add(param);

    ConfigTunnelJdbc confTunnelJdbc = builder.build();
    confTunnelJdbc.write("some/folder/test-config.hotconf", conf);

    Conf readConf = confTunnelJdbc.read("some/folder/test-config.hotconf");
    assertThat(readConf).isNotNull();
    assertThat(readConf.params.get(0).valueStr).isEqualTo("value0");

    execSql(connectionGet, "DROP SCHEMA " + schemaName + " CASCADE");
  }

  /**
   * The error of a lost race is swallowed only when the wanted object is there anyway. This covers the errors which cannot be
   * known by their code - as {@code tuple concurrently updated} of two {@code CREATE OR REPLACE FUNCTION} at once.
   */
  @Test(dataProvider = "databaseType")
  public void isMadeByOther_looksAtTheObjectNotOnlyAtTheError(@NonNull DatabaseType databaseType) {

    ConnectionGet connectionGet = createConnectionGet(databaseType, "isMadeByOther");

    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName("isMadeByOther_" + RND.str(8));

    ConfigTunnelJdbc confTunnelJdbc = builder.connectionGet(connectionGet).build();

    SQLException tupleConcurrentlyUpdated = new SQLException("ERROR: tuple concurrently updated", "XX000");

    // an error of an unknown kind, but the object is there - the winner of the race has already done this work
    assertThat(confTunnelJdbc.isMadeByOther(tupleConcurrentlyUpdated, () -> true)).isTrue();

    // the same error and no object - something really went wrong
    assertThat(confTunnelJdbc.isMadeByOther(tupleConcurrentlyUpdated, () -> false)).isFalse();

    // a known error of a race is enough by itself
    assertThat(confTunnelJdbc.isMadeByOther(new SQLException("relation already exists", "42P07"), () -> false)).isTrue();

    // a check which cannot be made means "not there"
    assertThat(confTunnelJdbc.isMadeByOther(tupleConcurrentlyUpdated, () -> {
      throw new SQLException("current transaction is aborted", "25P02");
    })).isFalse();
  }

  /**
   * How many instances of the application start at the same moment in the tests of the races.
   */
  private static final int INSTANCE_COUNT = 8;

  private @NonNull ConfigTunnelJdbc newTunnel(@NonNull ConnectionGet connectionGet, @NonNull String tableName) {
    ConfigTunnelJdbcBuilder builder = ConfigTunnelJdbc.builder();
    builder.tableName(tableName);
    builder.connectionGet(connectionGet);
    return builder.build();
  }

  /**
   * A tunnel whose check of the table always says "there is no table" - the state of an instance which lost the race of creation.
   */
  private @NonNull ConfigTunnelJdbc tunnelWhichSeesNoTable(@NonNull DatabaseType databaseType,
                                                           @NonNull ConnectionGet connectionGet,
                                                           @NonNull ConfigTunnelJdbc.Def def) {
    return switch (databaseType) {
      case PostgreSQL -> new ConfigTunnelJdbcPg(connectionGet, def) {
        @Override protected boolean tableExists(@NonNull Connection connection) {
          return false;
        }
      };
      case MariaDB -> new ConfigTunnelJdbcMariaDb(connectionGet, def) {
        @Override protected boolean tableExists(@NonNull Connection connection) {
          return false;
        }
      };
    };
  }

  /**
   * A tunnel whose checks always say "there is no such object" - the state of an instance which lost every race of creation.
   */
  private @NonNull ConfigTunnelJdbc tunnelWhichSeesNothing(@NonNull DatabaseType databaseType,
                                                           @NonNull ConnectionGet connectionGet,
                                                           @NonNull ConfigTunnelJdbc.Def def) {
    return switch (databaseType) {
      case PostgreSQL -> new ConfigTunnelJdbcPg(connectionGet, def) {
        @Override protected boolean schemaExists(@NonNull Connection connection) {
          return false;
        }

        @Override protected boolean tableExists(@NonNull Connection connection) {
          return false;
        }

        @Override protected boolean triggerExists(@NonNull Connection connection) {
          return false;
        }
      };
      case MariaDB -> new ConfigTunnelJdbcMariaDb(connectionGet, def) {
        @Override protected boolean schemaExists(@NonNull Connection connection) {
          return false;
        }

        @Override protected boolean tableExists(@NonNull Connection connection) {
          return false;
        }

        @Override protected boolean triggerExists(@NonNull Connection connection) {
          return false;
        }
      };
    };
  }

}
