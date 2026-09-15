package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class ConfigTunnelJdbcMariaDb extends ConfigTunnelJdbc {
  ConfigTunnelJdbcMariaDb(@NonNull ConnectionGet connectionGet, @NonNull Def def) {
    super(connectionGet, def);
  }

  @Override public void createTableIfNotExists() {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      createTableIfMissing(connection);

      addOrderColumnIfMissing(connection);

      createTriggerIfNotExists(connection);
    } catch (SQLException e) {
      throw new RuntimeException("O1p2Q3r4S5 :: Could not create configuration table: " + def.tableName, e);
    }
  }

  @Override protected boolean tableExists(@NonNull Connection connection) throws SQLException {
    String sql = """
      SELECT 1
      FROM information_schema.tables
      WHERE table_schema = COALESCE(?, DATABASE()) AND table_name = ?
      """;

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, schemaName());
      ps.setString(2, simpleTableName());
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  @Override protected boolean schemaExists(@NonNull Connection connection) throws SQLException {
    String sql = """
      SELECT 1
      FROM information_schema.schemata
      WHERE schema_name = ?
      """;

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, String.valueOf(schemaName()));
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  /**
   * Creates the table of the config. The table is known to be missing when this method is called.
   * <p>
   * The columns of the timestamps are of the type {@code TIMESTAMP}, and this type already keeps the moment itself: MariaDB
   * stores such a value in UTC and shows it in the time zone of the session. Because of this MariaDB needs no counterpart of the
   * change of the type which PostgreSQL needs - see {@code ConfigTunnelJdbcPg}, where the plain {@code TIMESTAMP} of an older
   * version of this library is brought to {@code TIMESTAMPTZ}; the type without the time zone in MariaDB is
   * {@code DATETIME}, and this library has never used it.
   */
  @Override protected void createTable(@NonNull Connection connection) throws SQLException {
    createSchemaIfNotExists(connection);
    String sql = """
      CREATE TABLE {tableName} (
        {colFolder}           VARCHAR(255) NOT NULL,
        {colConfigName}       VARCHAR(255) NOT NULL,
        {colParamName}        VARCHAR(255) NOT NULL,
        {colParamValueStr}    TEXT,
        {colComment}          TEXT,
        {colError}            TEXT,
        {colNotice}           TEXT,
        {colOrder}            INT NOT NULL DEFAULT 0,
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
      .replace("{colOrder}", def.colOrder)
      .replace("{colCreatedAt}", def.colCreatedAt)
      .replace("{colLastModified}", def.colLastModified);

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.executeUpdate();
    }
  }

  /**
   * The answer of MariaDB to {@code ON CONFLICT DO NOTHING}: the row which is already there is assigned to itself, which
   * changes nothing in it.
   * <p>
   * {@code INSERT IGNORE} would say the same in fewer words, but it turns every error of the statement into a warning - a value
   * too long for its column would be swallowed together with the duplicate key, and a row of the config would quietly go
   * missing. The no-op update swallows the duplicate key only.
   */
  @Override protected @NonNull String onConflictDoNothing() {
    return "ON DUPLICATE KEY UPDATE " + def.colFolder + " = " + def.colFolder;
  }

  /**
   * Creates the trigger which refreshes the last modification timestamp of the changed row, when the table has no such trigger yet.
   * <p>
   * The table of the config may exist without the trigger: the table could be created by an older version of this library or by
   * hand, and the trigger could be dropped later. Because of this the trigger is checked every time the table is checked, and
   * nothing is created when the trigger is already there with the expected body - a database which is already prepared needs no
   * rights for DDL at all.
   * <p>
   * MariaDB creates the trigger without a separate function, so {@code def.triggerFnName} is not used here.
   * <p>
   * Unlike PostgreSQL, the trigger does not touch the row of the whole config - the row where the parameter name is empty:
   * MariaDB forbids a trigger to change the table it is attached to (error 1442), so such an update would break
   * every update of the table. For MariaDB the change of the whole config is seen through the maximum of the last
   * modification timestamps of all rows of the config.
   */
  private void createTriggerIfNotExists(@NonNull Connection connection) throws SQLException {

    String triggerBody = triggerBody();
    String storedBody  = triggerExists(connection) ? triggerBodyStored(connection) : null;

    if (storedBody != null && triggerBody.strip().equals(storedBody.strip())) return;

    // OR REPLACE is needed only when the trigger is there with another body, and MariaDB has no other way to change
    // the body of a trigger. MariaDB does not accept OR REPLACE together with IF NOT EXISTS (error 1221),
    // so exactly one of them is used
    String createTrigger = storedBody == null ? "CREATE TRIGGER IF NOT EXISTS" : "CREATE OR REPLACE TRIGGER";

    try (Statement statement = connection.createStatement()) {
      String triggerSql = """
        {createTrigger} {triggerName}
        BEFORE UPDATE ON {tableName}
        FOR EACH ROW
        {triggerBody}
        """
        .replace("{createTrigger}", createTrigger)
        .replace("{triggerName}", withSchema(triggerName()))
        .replace("{tableName}", def.tableName)
        .replace("{triggerBody}", triggerBody);

      try {
        statement.execute(triggerSql);
      } catch (SQLException e) {
        if (!isMadeByOther(e, () -> triggerExists(connection))) throw e;
      }
    }
  }

  /**
   * The body of the trigger - the text after {@code FOR EACH ROW}.
   * <p>
   * MariaDB keeps this text as it is given, so the same text read back from {@code information_schema.triggers} means the trigger
   * in the database does exactly what this version of the library expects.
   */
  private @NonNull String triggerBody() {
    return ("SET NEW.{colLastModified} = CURRENT_TIMESTAMP").replace("{colLastModified}", def.colLastModified);
  }

  /**
   * The body of the trigger as it is stored in the database, or {@code null} when the table has no such trigger.
   */
  private @Nullable String triggerBodyStored(@NonNull Connection connection) throws SQLException {
    String sql = """
      SELECT action_statement
      FROM information_schema.triggers
      WHERE trigger_schema = COALESCE(?, DATABASE()) AND trigger_name = ? AND event_object_table = ?
      """;

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, schemaName());
      ps.setString(2, triggerName());
      ps.setString(3, simpleTableName());
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getString(1);
      }
    }
  }
}
