package kz.pompei.conf.jdbc;

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
    throw new RuntimeException("NfR0em2z3j :: Not impl yet ConfTunnelJdbcBuilder.detectDb()");
  }
}
