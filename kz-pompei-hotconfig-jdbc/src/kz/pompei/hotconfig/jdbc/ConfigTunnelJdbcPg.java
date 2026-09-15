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
      createTableIfMissing(connection);

      addOrderColumnIfMissing(connection);

      alterTimestampColumnsToTimestampTz(connection);

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

  @Override protected boolean schemaExists(@NonNull Connection connection) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("SELECT to_regnamespace(?) IS NOT NULL")) {
      ps.setString(1, String.valueOf(schemaName()));
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean(1);
      }
    }
  }

  @Override protected void createTable(@NonNull Connection connection) throws SQLException {
    createSchemaIfNotExists(connection);
    String sql = """
      CREATE TABLE {tableName} (
        {colFolder}          VARCHAR(1000) NOT NULL,
        {colConfigName}      VARCHAR(1000) NOT NULL,
        {colParamName}       VARCHAR(1000) NOT NULL,
        {colParamValueStr}   TEXT,
        {colComment}         TEXT,
        {colError}           TEXT,
        {colNotice}          TEXT,
        {colOrder}           INT NOT NULL DEFAULT 0,
        {colCreatedAt}       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
        {colLastModified}    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
   * {@code ON CONFLICT DO NOTHING} - the row which is already there stays as it is.
   * <p>
   * No columns of the conflict are named: without them PostgreSQL takes any unique index of the table, so a table made by hand
   * with another primary key works just as well. Naming the columns would be possible only for {@code DO UPDATE}, which this
   * library does not use - see {@link ConfigTunnelJdbc#onConflictDoNothing()}.
   */
  @Override protected @NonNull String onConflictDoNothing() {
    return "ON CONFLICT DO NOTHING";
  }

  /**
   * Brings the columns of the timestamps of the config table to {@code TIMESTAMPTZ} - {@code timestamp with time zone}.
   * <p>
   * A table made by an older version of this library keeps these columns as a plain {@code TIMESTAMP} - a wall clock without
   * any time zone. Such a value says nothing about the moment it means: the same text is read as another moment by an
   * application whose time zone differs from the one which wrote it, and the comparison of the last modification timestamp
   * with the clock of the application starts to lie. {@code TIMESTAMPTZ} keeps the moment itself, so every reader sees the
   * very same moment.
   * <p>
   * The type is checked every time the table is checked - just as the column of the row order and the trigger are - because the
   * table may come from an older version of this library or be made by hand. Nothing is changed when both columns already keep
   * the time zone, so a database which is already prepared needs no rights for DDL at all.
   * <p>
   * The values of the columns survive the change: PostgreSQL reads the old wall clock in the time zone of the session, which is
   * exactly how the old value was written and how this library used to read it back, so the moment of every row stays the same.
   */
  private void alterTimestampColumnsToTimestampTz(@NonNull Connection connection) throws SQLException {
    alterColumnToTimestampTzIfWithoutTimeZone(connection, def.colCreatedAt);
    alterColumnToTimestampTzIfWithoutTimeZone(connection, def.colLastModified);
  }

  /**
   * Changes the type of the specified column to {@code TIMESTAMPTZ}, when the column keeps a timestamp without a time zone.
   * <p>
   * Just as with the table itself, two instances of the application may change the type at the same moment, and the loser of
   * this race gets the error of the winner. The column is of the wanted type anyway, so only this error is swallowed.
   * <p>
   * The default of the column is not touched: PostgreSQL brings the default expression to the new type by itself, so
   * {@code CURRENT_TIMESTAMP} stays {@code CURRENT_TIMESTAMP}, and a column made by hand keeps the default it was given.
   */
  private void alterColumnToTimestampTzIfWithoutTimeZone(@NonNull Connection connection,
                                                         @NonNull String columnName) throws SQLException {

    if (!isTimestampWithoutTimeZone(connection, columnName)) return;

    String sql = """
      ALTER TABLE {tableName}
      ALTER COLUMN {columnName} TYPE TIMESTAMPTZ
      """
      .replace("{tableName}", def.tableName)
      .replace("{columnName}", columnName);

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.executeUpdate();
    } catch (SQLException e) {
      if (!isMadeByOther(e, () -> !isTimestampWithoutTimeZone(connection, columnName))) throw e;
    }
  }

  /**
   * Checks that the specified column of the config table keeps a timestamp without a time zone - the type this library used
   * before and the only type which is to be changed.
   * <p>
   * A column of another type - a timestamp with the time zone, a date, or anything else a hand-made table may keep - is left
   * alone: the owner of such a table knows better what belongs there.
   */
  private boolean isTimestampWithoutTimeZone(@NonNull Connection connection, @NonNull String columnName) throws SQLException {
    String sql = """
      SELECT 1
      FROM information_schema.columns
      WHERE LOWER(table_schema) = LOWER(COALESCE(?, CURRENT_SCHEMA()))
        AND LOWER(table_name) = LOWER(?)
        AND LOWER(column_name) = LOWER(?)
        AND data_type = 'timestamp without time zone'
      """;

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, schemaName());
      ps.setString(2, simpleTableName());
      ps.setString(3, columnName);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
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
   * <p>
   * Two instances of the application may create the function and the trigger at the very same moment, and the loser of this race
   * gets the error of the winner. Only this error is swallowed - the object is there, made by the winner from the very same
   * version of this library.
   */
  private void createTriggerIfNotExists(@NonNull Connection connection) throws SQLException {

    String functionName = triggerFnName();
    String functionBody = triggerFunctionBody();

    boolean triggerReady  = triggerExists(connection);
    boolean functionReady = sameSql(functionBody, functionSource(connection, functionName));

    if (triggerReady && functionReady) return;

    try (Statement statement = connection.createStatement()) {

      if (!functionReady) {
        try {
          statement.execute("CREATE OR REPLACE FUNCTION " + functionName + "()\nRETURNS trigger AS $$\n"
                              + functionBody + "$$ LANGUAGE plpgsql");
        } catch (SQLException e) {
          // two instances of the application may create the same function at the very same moment; the function of the winner
          // has the same body, because it comes from the same version of this library
          if (!isMadeByOther(e, () -> sameSql(functionBody, functionSource(connection, functionName)))) throw e;
        }
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
        if (!isMadeByOther(e, () -> triggerExists(connection))) throw e;
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
