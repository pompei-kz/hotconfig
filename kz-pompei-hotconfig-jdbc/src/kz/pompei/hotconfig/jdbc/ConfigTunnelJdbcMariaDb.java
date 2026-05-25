package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfigTunnelJdbcMariaDb extends ConfigTunnelJdbc {
  ConfigTunnelJdbcMariaDb(@NonNull ConnectionGet connectionGet, @NonNull Def def) {
    super(connectionGet, def);
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
          {colError}            TEXT,
          {colNotice}           TEXT,
          {colCreatedAt}        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          {colLastModified}     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          PRIMARY KEY ({colFolder}, {colConfigName}, {colParamName})
        )
        """
        .replace("{tableName}", def.tableName)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName)
        .replace("{colParamName}", def.colParamName)
        .replace("{colParamValueStr}", def.colParamValueStr)
        .replace("{colComment}", def.colComment)
        .replace("{colError}", def.colError)
        .replace("{colNotice}", def.colNotice)
        .replace("{colCreatedAt}", def.colCreatedAt)
        .replace("{colLastModified}", def.colLastModified);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("O1p2Q3r4S5 :: Could not create configuration table: " + def.tableName, e);
    }
  }
}
