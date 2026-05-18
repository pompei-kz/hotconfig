package kz.pompei.conf.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import lombok.NonNull;
import org.testng.annotations.Test;

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
            throw new SQLException("2m79Nm1WPK :: Connection OK");
          }
        }
      }
    }

  }

  @Test(dataProvider = "databaseType")
  public void read_existsInDbTable(@NonNull DatabaseType databaseType) {

    ConnectionGet connectionGet = createConnectionGet(databaseType, "read_existsInDbTable");

    ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
    def.tableName = "read_existsInDbTable";// this is name of this method

    createsTable(connectionGet, def);

    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "", "", "This is comment for conf\nline 2\nline 3");
    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "param0", "value0", "This is test0\nsecond line\nanother line");
    insertRow(connectionGet, def, "some/folder", "test-config.hotconf", "param1", "value1", "This is test1\nsecond line\nanother line");

    ConfTunnelJdbc confTunnelJdbc = ConfTunnelJdbcBuilder.detectDbAndCreate(connectionGet, def);

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
}
