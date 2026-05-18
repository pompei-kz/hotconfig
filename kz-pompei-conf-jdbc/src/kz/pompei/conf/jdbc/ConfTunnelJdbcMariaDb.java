package kz.pompei.conf.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfTunnelJdbcMariaDb extends ConfTunnelJdbc {
  public ConfTunnelJdbcMariaDb(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    super(connectionGet, params);
  }

  @Override public void createTableIfNotExists() {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      createSchemaIfNotExists(connection);
      try (PreparedStatement ps = connection.prepareStatement("""
        CREATE TABLE IF NOT EXISTS %s (
          %s VARCHAR(1000) NOT NULL,
          %s VARCHAR(500) NOT NULL,
          %s VARCHAR(100) NOT NULL,
          %s TEXT,
          %s TEXT,
          %s TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          %s TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (%s, %s, %s)
        )
        """.formatted(
        params.tableName,
        params.colFolder,
        params.colConfigName,
        params.colParamName,
        params.colParamValueStr,
        params.colComment,
        params.colCreatedAt,
        params.colLastModified,
        params.colFolder,
        params.colConfigName,
        params.colParamName
      ))) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("W3tY7uI9oP :: Could not create configuration table: " + params.tableName, e);
    }
  }
}
