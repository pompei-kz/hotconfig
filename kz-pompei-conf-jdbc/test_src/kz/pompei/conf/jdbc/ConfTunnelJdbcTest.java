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
