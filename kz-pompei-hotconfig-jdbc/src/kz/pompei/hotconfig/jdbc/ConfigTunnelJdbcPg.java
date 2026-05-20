package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import lombok.NonNull;

public class ConfigTunnelJdbcPg extends ConfigTunnelJdbc {
  public ConfigTunnelJdbcPg(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    super(connectionGet, params);
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
          {colCreatedAt}       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          {colLastModified}    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
          .replace("{colLastModified}", params.colLastModified);

        statement.execute(functionSql);
        statement.execute("DROP TRIGGER IF EXISTS " + triggerName + " ON " + params.tableName);
        String triggerSql = """
          CREATE TRIGGER {triggerName}
          BEFORE UPDATE ON {tableName}
          FOR EACH ROW
          EXECUTE FUNCTION {functionName}()
          """
          .replace("{triggerName}", triggerName)
          .replace("{functionName}", functionName)
          .replace("{tableName}", params.tableName);

        statement.execute(triggerSql);
      }
    } catch (SQLException e) {
      throw new RuntimeException("T6u7V8w9X0 :: Could not create configuration table: " + params.tableName, e);
    }
  }

  private String triggerFunctionName() {
    return "fn_" + params.tableName.replaceAll("[^A-Za-z0-9]+", "_") + "_last_modified";
  }

  private String triggerName() {
    return "trg_" + params.tableName.replaceAll("[^A-Za-z0-9]+", "_") + "_last_modified";
  }
}
