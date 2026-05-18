package kz.pompei.conf.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import org.testng.annotations.Test;

public class ConfTunnelJdbcTest extends JdbcTestParent {

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

}
