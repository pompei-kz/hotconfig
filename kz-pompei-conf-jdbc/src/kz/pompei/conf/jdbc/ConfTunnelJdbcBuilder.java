package kz.pompei.conf.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfTunnelJdbcBuilder {

  public static @NonNull ConfTunnelJdbc detectDbAndCreate(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {

    DatabaseType databaseType = detectDb(connectionGet);

    return switch (databaseType) {
      case PostgreSQL -> new ConfTunnelJdbcPg(connectionGet, params);
      case MariaDB -> new ConfTunnelJdbcMariaDb(connectionGet, params);
    };
  }

  private static @NonNull DatabaseType detectDb(@NonNull ConnectionGet connectionGet) throws UnknownDb {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      if (productName.contains("PostgreSQL")) return DatabaseType.PostgreSQL;
      if (productName.contains("MariaDB")) return DatabaseType.MariaDB;
      throw new UnknownDb();
    } catch (SQLException e) {
      throw new RuntimeException("NfR0em2z3j :: Could not detect database type", e);
    }
  }
}
