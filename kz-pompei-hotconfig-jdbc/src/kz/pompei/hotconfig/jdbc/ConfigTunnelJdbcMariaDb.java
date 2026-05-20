package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfigTunnelJdbcMariaDb extends ConfigTunnelJdbc {
  public ConfigTunnelJdbcMariaDb(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    super(connectionGet, params);
  }

  @Override public void createTableIfNotExists() {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      createSchemaIfNotExists(connection);
      String sql = """
        CREATE TABLE IF NOT EXISTS {tableName} (
          {colFolder}           VARCHAR(255) NOT NULL,
          {colConfigName}       VARCHAR(255) NOT NULL,
          {colParamName}        VARCHAR(255) NOT NULL,
          {colParamValueStr}    TEXT,
          {colComment}          TEXT,
          {colCreatedAt}        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          {colLastModified}     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          PRIMARY KEY ({colFolder}, {colConfigName}, {colParamName})
        )
        """
        .replace("{tableName}", params.tableName)
        .replace("{colFolder}", params.colFolder)
        .replace("{colConfigName}", params.colConfigName)
        .replace("{colParamName}", params.colParamName)
        .replace("{colParamValueStr}", params.colParamValueStr)
        .replace("{colComment}", params.colComment)
        .replace("{colCreatedAt}", params.colCreatedAt)
        .replace("{colLastModified}", params.colLastModified);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("O1p2Q3r4S5 :: Could not create configuration table: " + params.tableName, e);
    }
  }
}
