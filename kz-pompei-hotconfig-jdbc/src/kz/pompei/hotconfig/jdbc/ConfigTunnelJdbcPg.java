package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfigTunnelJdbcPg extends ConfigTunnelJdbc {
  ConfigTunnelJdbcPg(@NonNull ConnectionGet connectionGet, @NonNull ConfigTunnelJdbc.Def def) {
    super(connectionGet, def);
  }

  @Override public void createTableIfNotExists() {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      createSchemaIfNotExists(connection);
      String sql = """
        CREATE TABLE IF NOT EXISTS {tableName} (
          {colFolder}          VARCHAR(1000) NOT NULL,
          {colConfigName}      VARCHAR(1000) NOT NULL,
          {colParamName}       VARCHAR(1000) NOT NULL,
          {colParamValueStr}   TEXT,
          {colComment}         TEXT,
          {colError}           TEXT,
          {colNotice}          TEXT,
          {colCreatedAt}       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          {colLastModified}    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

      String functionName = triggerFunctionName();
      String triggerName = triggerName();

      try (Statement statement = connection.createStatement()) {
        String functionSql = """
          CREATE OR REPLACE FUNCTION {functionName}()
          RETURNS trigger AS $$
          BEGIN
            NEW.{colLastModified} = CURRENT_TIMESTAMP;
            RETURN NEW;
          END;
          $$ LANGUAGE plpgsql
          """
          .replace("{functionName}", functionName)
          .replace("{colLastModified}", def.colLastModified);

        statement.execute(functionSql);
        statement.execute("DROP TRIGGER IF EXISTS " + triggerName + " ON " + def.tableName);
        String triggerSql = """
          CREATE TRIGGER {triggerName}
          BEFORE UPDATE ON {tableName}
          FOR EACH ROW
          EXECUTE FUNCTION {functionName}()
          """
          .replace("{triggerName}", triggerName)
          .replace("{functionName}", functionName)
          .replace("{tableName}", def.tableName);

        statement.execute(triggerSql);
      }
    } catch (SQLException e) {
      throw new RuntimeException("T6u7V8w9X0 :: Could not create configuration table: " + def.tableName, e);
    }
  }

  private String triggerFunctionName() {
    return "fn_" + def.tableName.replaceAll("[^A-Za-z0-9]+", "_") + "_last_modified";
  }

  private String triggerName() {
    return "trg_" + def.tableName.replaceAll("[^A-Za-z0-9]+", "_") + "_last_modified";
  }
}
