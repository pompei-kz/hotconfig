package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class ConfigTunnelJdbcPg extends ConfigTunnelJdbc {
  ConfigTunnelJdbcPg(@NonNull ConnectionGet connectionGet, @NonNull ConfigTunnelJdbc.Def def) {
    super(connectionGet, def);
  }

  @Override public void createTableIfNotExists() {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      if (!tableExists(connection)) createTable(connection);

      addOrderColumnIfMissing(connection);

      createTriggerIfNotExists(connection);
    } catch (SQLException e) {
      throw new RuntimeException("T6u7V8w9X0 :: Could not create configuration table: " + def.tableName, e);
    }
  }

  @Override protected boolean tableExists(@NonNull Connection connection) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
      ps.setString(1, def.tableName);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean(1);
      }
    }
  }

  private void createTable(@NonNull Connection connection) throws SQLException {
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
        {colOrder}           INT NOT NULL DEFAULT 0,
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
      .replace("{colOrder}", def.colOrder)
      .replace("{colCreatedAt}", def.colCreatedAt)
      .replace("{colLastModified}", def.colLastModified);

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.executeUpdate();
    }
  }

  /**
   * Creates the trigger which refreshes the last modification timestamp, when the table has no such trigger yet.
   * <p>
   * The trigger refreshes the timestamp of the changed row and, when the changed row is a parameter row,
   * of the row of the whole config as well - the row where the parameter name is empty. Because of this,
   * a change of a single parameter is seen as a change of the whole config file.
   * <p>
   * The table of the config may exist without the trigger: the table could be created by an older version of this library or by
   * hand, and the trigger could be dropped later. Because of this the trigger is checked every time the table is checked.
   * <p>
   * Nothing is created when the trigger is already there and its function keeps the expected body, so a database which is already
   * prepared needs no rights for DDL at all. An existing trigger is never dropped: the whole body of the trigger lives in the
   * function, so replacing the function is enough to bring an existing trigger up to date, and dropping the trigger would leave a
   * moment when changes of the table are not caught by anybody.
   */
  private void createTriggerIfNotExists(@NonNull Connection connection) throws SQLException {

    String functionName = triggerFnName();
    String functionBody = triggerFunctionBody();

    boolean triggerReady  = triggerExists(connection);
    boolean functionReady = sameSql(functionBody, functionSource(connection, functionName));

    if (triggerReady && functionReady) return;

    try (Statement statement = connection.createStatement()) {

      if (!functionReady) {
        statement.execute("CREATE OR REPLACE FUNCTION " + functionName + "()\nRETURNS trigger AS $$\n"
                            + functionBody + "$$ LANGUAGE plpgsql");
      }

      if (triggerReady) return;

      String triggerSql = """
        CREATE TRIGGER {triggerName}
        BEFORE UPDATE ON {tableName}
        FOR EACH ROW
        EXECUTE FUNCTION {functionName}()
        """
        .replace("{triggerName}", triggerName())
        .replace("{functionName}", functionName)
        .replace("{tableName}", def.tableName);

      try {
        statement.execute(triggerSql);
      } catch (SQLException e) {
        if (!isDuplicateObject(e)) throw e;
      }
    }
  }

  /**
   * The body of the trigger function - the text between the dollar quotes of {@code CREATE FUNCTION}.
   * <p>
   * PostgreSQL keeps this text as it is given, so the same text read back from {@code pg_proc.prosrc} means the function
   * in the database does exactly what this version of the library expects.
   */
  private @NonNull String triggerFunctionBody() {
    return """
      BEGIN
        NEW.{colLastModified} = CURRENT_TIMESTAMP;

        IF NEW.{colParamName} IS NOT NULL AND NEW.{colParamName} <> '' THEN
          UPDATE {tableName}
             SET {colLastModified} = CURRENT_TIMESTAMP
           WHERE {colFolder} = NEW.{colFolder}
             AND {colConfigName} = NEW.{colConfigName}
             AND {colParamName} = ''
             AND {colLastModified} <> CURRENT_TIMESTAMP;
        END IF;

        RETURN NEW;
      END;
      """
      .replace("{tableName}", def.tableName)
      .replace("{colFolder}", def.colFolder)
      .replace("{colConfigName}", def.colConfigName)
      .replace("{colParamName}", def.colParamName)
      .replace("{colLastModified}", def.colLastModified);
  }

  /**
   * The body of the function with the specified name as it is stored in the database, or {@code null} when there is no such function.
   */
  private @Nullable String functionSource(@NonNull Connection connection, @NonNull String functionName) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("SELECT prosrc FROM pg_proc WHERE oid = to_regprocedure(?)")) {
      ps.setString(1, functionName + "()");
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getString(1);
      }
    }
  }

  private boolean sameSql(@NonNull String expected, @Nullable String actual) {
    return actual != null && expected.strip().equals(actual.strip());
  }
}
