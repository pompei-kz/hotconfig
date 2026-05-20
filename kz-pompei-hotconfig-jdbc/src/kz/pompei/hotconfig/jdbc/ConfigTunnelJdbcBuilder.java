package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfigTunnelJdbcBuilder {

  public static @NonNull ConfigTunnelJdbc build(@NonNull ConnectionGet connectionGet, @NonNull ConfigTunnelJdbcDef params) {

    DatabaseType databaseType = detectDb(connectionGet);

    return switch (databaseType) {
      case PostgreSQL -> new ConfigTunnelJdbcPg(connectionGet, params);
      case MariaDB -> new ConfigTunnelJdbcMariaDb(connectionGet, params);
    };
  }

  private static @NonNull DatabaseType detectDb(@NonNull ConnectionGet connectionGet) throws UnknownDb {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      if (productName.contains("PostgreSQL")) return DatabaseType.PostgreSQL;
      if (productName.contains("MariaDB")) return DatabaseType.MariaDB;
      throw new UnknownDb("Foh9XKIbpC :: productName = "+productName);
    } catch (SQLException e) {
      throw new RuntimeException("U1v2W3x4Y5 :: Could not detect database type", e);
    }
  }
}
