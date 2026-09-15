package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import kz.pompei.hotconfig.core.ConfigTunnel;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * Stores configuration data in a database.
 * <p>
 * Configurations are stored in a dedicated table named by {@code params.tableName}.
 * <p>
 * If the table does not exist, it is created automatically on first access.
 *
 */
public abstract class ConfigTunnelJdbc implements ConfigTunnel {

  @NonNull final Def           def;
  @NonNull final ConnectionGet connectionGet;

  public static @NonNull ConfigTunnelJdbcBuilder builder() {
    return new ConfigTunnelJdbcBuilder();
  }

  ConfigTunnelJdbc(@NonNull ConnectionGet connectionGet, @NonNull Def def) {
    this.connectionGet = connectionGet;
    this.def           = def;
  }

  @RequiredArgsConstructor
  static class Def {
    final String colFolder;
    final String tableName;
    final String triggerName;
    final String triggerFnName;
    final String colConfigName;
    final String colParamName;
    final String colParamValueStr;
    final String colComment;
    final String colError;
    final String colNotice;
    final String colCreatedAt;
    final String colLastModified;
    final String colOrder;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      addOrderColumnIfMissing(connection);

      String sql = """
        SELECT {colParamName}, {colParamValueStr}, {colComment}, {colError}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        ORDER BY {colOrder}, {colParamName}
        """
        .replace("{colParamName}", def.colParamName)
        .replace("{colParamValueStr}", def.colParamValueStr)
        .replace("{colComment}", def.colComment)
        .replace("{colError}", def.colError)
        .replace("{colOrder}", def.colOrder)
        .replace("{tableName}", def.tableName)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);

        try (ResultSet rs = ps.executeQuery()) {
          Conf    conf = new Conf();
          boolean ok   = false;
          while (rs.next()) {
            ok = true;
            String paramName = rs.getString(def.colParamName);
            String comment   = rs.getString(def.colComment);
            if (paramName == null || paramName.isEmpty()) {
              conf.confComments.addAll(commentLines(comment));
            } else {
              ConfParam param = new ConfParam();
              param.name     = paramName;
              param.valueStr = rs.getString(def.colParamValueStr);
              param.error    = rs.getString(def.colError);
              param.comments.addAll(commentLines(comment));
              conf.params.add(param);
            }
          }
          return ok ? conf : null;
        }
      }
    } catch (SQLException e) {
      if (isMissingTable(e)) return null;
      throw new RuntimeException("Z6a7B8c9D0 :: Could not read configuration from table: " + def.tableName, e);
    }
  }

  @Override public void write(@NonNull String localPath, @NonNull Conf conf) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    createTableIfNotExistsOnce();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      insertRow(connection, folder, configName, "", "", String.join("\n", conf.confComments), null, null, 0);
      for (int i = 0; i < conf.params.size(); i++) {
        ConfParam param = conf.params.get(i);
        insertRow(connection, folder, configName, param.name, param.valueStr, String.join("\n", param.comments), param.error, null, i + 1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("E1f2G3h4I5 :: Could not write configuration to table: " + def.tableName, e);
    }
  }

  @Override public @NonNull List<String> readNoticeLines(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colNotice}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ? AND {colParamName} = ?
        """
        .replace("{colNotice}", def.colNotice)
        .replace("{tableName}", def.tableName)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName)
        .replace("{colParamName}", def.colParamName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.setString(3, "");
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return List.of();
          return lines(rs.getString(def.colNotice));
        }
      }
    } catch (SQLException e) {
      if (isMissingTable(e)) return List.of();
      throw new RuntimeException("V1w2X3y4Z5 :: Could not read configuration notice from table: " + def.tableName, e);
    }
  }

  @Override public void writeNoticeLines(@NonNull String localPath, @NonNull List<String> lines) {
    String folder     = folder(localPath);
    String configName = configName(localPath);
    String notice     = String.join("\n", lines);

    createTableIfNotExistsOnce();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String updateSql = """
        UPDATE {tableName}
        SET {colNotice} = ?
        WHERE {colFolder} = ? AND {colConfigName} = ? AND {colParamName} = ?
        """
        .replace("{tableName}", def.tableName)
        .replace("{colNotice}", def.colNotice)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName)
        .replace("{colParamName}", def.colParamName);

      if (updateNotice(connection, updateSql, notice, folder, configName)) return;

      // the row of the whole config is not there yet - the insert brings it with the notice inside
      if (insertRow(connection, folder, configName, "", null, null, null, notice, 0)) return;

      // another instance made the row at the very same moment, and its row carries no notice of ours: the row is there now,
      // so the update which found nothing a moment ago finds it
      updateNotice(connection, updateSql, notice, folder, configName);
    } catch (SQLException e) {
      throw new RuntimeException("A6b7C8d9E0 :: Could not write configuration notice to table: " + def.tableName, e);
    }
  }

  /**
   * Writes the notice into the row of the whole config.
   *
   * @return {@code true} when there was such a row and it took the notice
   */
  private boolean updateNotice(@NonNull Connection connection,
                               @NonNull String updateSql,
                               @NonNull String notice,
                               @NonNull String folder,
                               @NonNull String configName) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
      ps.setString(1, notice);
      ps.setString(2, folder);
      ps.setString(3, configName);
      ps.setString(4, "");
      return ps.executeUpdate() > 0;
    }
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    createTableIfNotExistsOnce();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT MAX({colLastModified})
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        """
        .replace("{colLastModified}", def.colLastModified)
        .replace("{tableName}", def.tableName)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return null;
          Timestamp timestamp = rs.getTimestamp(1);
          if (timestamp == null) return null;
          return timestamp.toInstant().toEpochMilli();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("J6k7L8m9N0 :: Could not get configuration modification time from table: " + def.tableName, e);
    }
  }

  private final AtomicBoolean tableCreated = new AtomicBoolean(false);

  private void createTableIfNotExistsOnce() {

    if (tableCreated.get()) return;

    synchronized (tableCreated) {
      if (tableCreated.get()) return;

      createTableIfNotExists();

      tableCreated.set(true);
    }
  }

  public abstract void createTableIfNotExists();

  /**
   * Checks that the table of the config already exists.
   * <p>
   * The check is asked before any DDL of the table is run, so a database user which may only read and write the data of an
   * existing table is enough for the work of the tunnel.
   */
  protected abstract boolean tableExists(@NonNull Connection connection) throws SQLException;

  /**
   * Creates the table of the config, together with its schema. The table is known to be missing when this method is called.
   */
  protected abstract void createTable(@NonNull Connection connection) throws SQLException;

  /**
   * Creates the table of the config when there is no such table yet.
   * <p>
   * Two instances of the application may start at the very same moment: both ask {@link #tableExists(Connection)}, both see no
   * table and both start creating it. The check and the creation cannot be made one atomic step, so the loser of this race gets
   * an error instead of the table. The error is swallowed only when the table is there anyway - see
   * {@link #isMadeByOther(SQLException, SqlCheck)} - and every other error goes up as it is.
   */
  protected void createTableIfMissing(@NonNull Connection connection) throws SQLException {
    if (tableExists(connection)) return;

    try {
      createTable(connection);
    } catch (SQLException e) {
      if (!isMadeByOther(e, () -> tableExists(connection))) throw e;
    }
  }

  /**
   * A check of the database which may fail with an error of the database.
   */
  @FunctionalInterface
  protected interface SqlCheck {
    boolean check() throws SQLException;
  }

  /**
   * Checks that the object which the failed statement wanted is there anyway, made by the instance which won the race.
   * <p>
   * The error of a lost race comes in more shapes than one: "such an object already exists", the unique index of the catalog of
   * PostgreSQL, and, for two {@code CREATE OR REPLACE FUNCTION} at once, {@code tuple concurrently updated}. Instead of trying to
   * know every one of them, the wanted object is asked for: when it is there, the work of this instance is already done by the
   * other one, and only then the error is not a failure. A known error of a race - see {@link #isDuplicateObject(SQLException)} -
   * is enough by itself, and a check which cannot be made means "not there", so the error goes up.
   */
  protected boolean isMadeByOther(@NonNull SQLException e, @NonNull SqlCheck check) {
    if (isDuplicateObject(e)) return true;

    try {
      return check.check();
    } catch (SQLException ignore) {
      return false;
    }
  }

  private final AtomicBoolean schemaCreated = new AtomicBoolean(false);

  /**
   * Creates the schema of the configuration table when there is no such schema yet.
   * <p>
   * The schema has its own race of several instances which start at the same moment, and it is handled here, at the level of the
   * schema itself: the error of a lost race is swallowed only when the schema is there anyway. Leaving this error to the caller
   * would be wrong - the table of the config is created after the schema, so an error swallowed above would skip the table.
   */
  protected void createSchemaIfNotExists(@NonNull Connection connection) throws SQLException {
    if (schemaName() == null) return;

    if (schemaCreated.get()) return;

    synchronized (schemaCreated) {

      if (schemaCreated.get()) return;

      if (!schemaExists(connection)) createSchema(connection);

      schemaCreated.set(true);
    }
  }

  private void createSchema(@NonNull Connection connection) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("CREATE SCHEMA " + schemaName())) {
      ps.executeUpdate();
    } catch (SQLException e) {
      if (!isMadeByOther(e, () -> schemaExists(connection))) throw e;
    }
  }

  /**
   * Checks that the schema of the configuration table already exists.
   * <p>
   * Asked before the DDL of the schema is run, so a database user which may only use an existing schema is enough.
   */
  protected abstract boolean schemaExists(@NonNull Connection connection) throws SQLException;

  /**
   * The name of the schema the configuration table belongs to, or {@code null} when {@code def.tableName} has no schema part.
   */
  protected @Nullable String schemaName() {
    int dotIndex = def.tableName.indexOf('.');
    if (dotIndex < 0) return null;
    return def.tableName.substring(0, dotIndex);
  }

  /**
   * The name of the configuration table without the schema part.
   */
  protected @NonNull String simpleTableName() {
    int dotIndex = def.tableName.indexOf('.');
    if (dotIndex < 0) return def.tableName;
    return def.tableName.substring(dotIndex + 1);
  }

  /**
   * The name of the trigger: the percent sign (%) of the template is replaced with the table name.
   * <p>
   * The name is not qualified with the schema.
   */
  protected @NonNull String triggerName() {
    return def.triggerName.replace("%", simpleTableName());
  }

  /**
   * The name of the trigger function: the percent sign (%) of the template is replaced with the table name.
   * <p>
   * The name is qualified with the schema of the table when the table has one.
   */
  protected @NonNull String triggerFnName() {
    return withSchema(def.triggerFnName.replace("%", simpleTableName()));
  }

  /**
   * Qualifies the name with the schema of the configuration table, when the table has a schema.
   */
  protected @NonNull String withSchema(@NonNull String name) {
    String schema = schemaName();
    return schema == null ? name : schema + "." + name;
  }

  /**
   * Checks that the trigger {@link #triggerName()} is already attached to the configuration table.
   * <p>
   * The table of the config may exist without the trigger: the table could be created by an older version of this library,
   * by hand, or the trigger could be dropped later. Because of this the trigger is never taken for granted - it is checked
   * every time the table is checked.
   */
  protected boolean triggerExists(@NonNull Connection connection) throws SQLException {
    String schema = schemaName();

    String sql = """
      SELECT 1
      FROM information_schema.triggers
      WHERE LOWER(trigger_name) = LOWER(?) AND LOWER(event_object_table) = LOWER(?)
      """;
    if (schema != null) sql = sql + "  AND LOWER(event_object_schema) = LOWER(?)\n";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, triggerName());
      ps.setString(2, simpleTableName());
      if (schema != null) ps.setString(3, schema);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  /**
   * Checks that the error means "such an object already exists" - the plainest of the errors two instances of the application get
   * from each other when both create the same table, schema, column, trigger or function at the very same moment.
   * <p>
   * The loser of such a race needs no more work: the object it wanted is already there, made by the winner. An error of another
   * kind is judged by the state of the database - see {@link #isMadeByOther(SQLException, SqlCheck)}.
   */
  protected boolean isDuplicateObject(@NonNull SQLException e) {
    SQLException current = e;
    while (current != null) {
      if (isDuplicateObjectSingle(current)) return true;
      current = current.getNextException();
    }
    return false;
  }

  private boolean isDuplicateObjectSingle(@NonNull SQLException e) {
    String sqlState = e.getSQLState();

    // PostgreSQL: duplicate_table, duplicate_schema, duplicate_column, duplicate_object, duplicate_function
    if ("42P07".equals(sqlState)
      || "42P06".equals(sqlState)
      || "42701".equals(sqlState)
      || "42710".equals(sqlState)
      || "42723".equals(sqlState)) {
      return true;
    }

    // MariaDB: table exists, database exists, column exists, trigger exists
    int errorCode = e.getErrorCode();
    if (errorCode == 1050 || errorCode == 1007 || errorCode == 1060 || errorCode == 1359) return true;

    // PostgreSQL sees the collision of two CREATE statements as a unique violation inside its own catalog, for example
    // "duplicate key value violates unique constraint pg_type_typname_nsp_index". A unique violation on the data of the
    // application means something else, so only the indexes of the catalog count here.
    if ("23505".equals(sqlState)) return isCatalogIndex(e.getMessage());

    return false;
  }

  private boolean isCatalogIndex(@Nullable String message) {
    if (message == null) return false;
    return message.contains("pg_type_typname_nsp_index")
      || message.contains("pg_class_relname_nsp_index")
      || message.contains("pg_namespace_nspname_index")
      || message.contains("pg_attribute_relid_attnam_index")
      || message.contains("pg_proc_proname_args_nsp_index")
      || message.contains("pg_trigger_tgrelid_tgname_index");
  }

  /**
   * The tail of the {@code INSERT} which makes a row that is already there leave the statement alone, instead of breaking it
   * with a violation of the primary key.
   * <p>
   * Two instances of the application may write the very same configuration at the very same moment. This is exactly what
   * happens on the first start on a new database: a value which is not in the table yet is written back with its default, so
   * every instance writes the same configuration into the same empty table at once. The check "is the row there" and the
   * insert of the row cannot be made one atomic step, so the loser of this race used to get a violation of the primary key
   * instead of a written row.
   * <p>
   * The row of the winner is left as it is, and not brought to the values of the loser. Refreshing it would mean an
   * {@code UPDATE}, an {@code UPDATE} wakes the trigger of the table, and the trigger of PostgreSQL updates a second row - the
   * row of the whole config - inside the very same statement. Two instances which hold one of these two rows each and want the
   * other one deadlock, and PostgreSQL kills one of them. A row which is already there holds the same values anyway: the
   * instances of this race write the same defaults.
   *
   * @see #insertRow(Connection, String, String, String, String, String, String, String, int)
   */
  protected abstract @NonNull String onConflictDoNothing();

  /**
   * Adds the column of the row order to a table which was made by an older version of this library.
   * <p>
   * Just as with the table itself, two instances of the application may add the column at the same moment, and the loser of this
   * race gets the error of the winner. The column is there, so only this error is swallowed.
   */
  protected void addOrderColumnIfMissing(@NonNull Connection connection) throws SQLException {
    if (columnExists(connection, def.colOrder)) return;

    String sql = """
      ALTER TABLE {tableName}
      ADD COLUMN {colOrder} INT NOT NULL DEFAULT 0
      """
      .replace("{tableName}", def.tableName)
      .replace("{colOrder}", def.colOrder);

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.executeUpdate();
    } catch (SQLException e) {
      if (!isMadeByOther(e, () -> columnExists(connection, def.colOrder))) throw e;
    }
  }

  private @NonNull String folder(@NonNull String localPath) {
    int slashIndex = localPath.lastIndexOf('/');
    if (slashIndex < 0) return "";
    return localPath.substring(0, slashIndex);
  }

  private @NonNull String configName(@NonNull String localPath) {
    int slashIndex = localPath.lastIndexOf('/');
    if (slashIndex < 0) return localPath;
    return localPath.substring(slashIndex + 1);
  }

  private List<String> commentLines(@Nullable String comment) {
    return lines(comment);
  }

  private List<String> lines(@Nullable String comment) {
    if (comment == null || comment.isEmpty()) return List.of();
    return Arrays.asList(comment.split("\\R", -1));
  }

  private boolean isMissingTable(@NonNull SQLException e) {
    SQLException current = e;
    while (current != null) {
      String sqlState  = current.getSQLState();
      int    errorCode = current.getErrorCode();
      String message   = current.getMessage();
      if ("42P01".equals(sqlState) || "42S02".equals(sqlState) || errorCode == 1146) return true;
      if (message != null && (
        message.contains("does not exist")
          || message.contains("Unknown table")
          || message.contains("Base table or view not found")
      )) {
        return true;
      }
      current = current.getNextException();
    }
    return false;
  }

  private boolean columnExists(@NonNull Connection connection, @NonNull String columnName) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    String           schema   = schemaName();
    String           table    = simpleTableName();

    try (ResultSet rs = metaData.getColumns(null, schema, table, columnName)) {
      if (rs.next()) return true;
    }

    try (ResultSet rs = metaData.getColumns(null, null, "%", "%")) {
      while (rs.next()) {
        String currentTableName  = rs.getString("TABLE_NAME");
        String currentColumnName = rs.getString("COLUMN_NAME");
        if (currentTableName != null
          && currentColumnName != null
          && currentTableName.equalsIgnoreCase(table)
          && currentColumnName.equalsIgnoreCase(columnName)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Inserts one row of the configuration, and leaves the row which is already there as it is - see
   * {@link #onConflictDoNothing()}.
   *
   * @return {@code true} when the row was really inserted, {@code false} when the row was already there. Both dialects answer
   *         an insert of one row with one changed row, and a row which was left alone with something else: PostgreSQL with
   *         zero, MariaDB with zero or two - two when the trigger of the table refreshed the timestamp of the row it left.
   */
  private boolean insertRow(@NonNull Connection connection,
                         @NonNull String folder,
                         @NonNull String configName,
                         @NonNull String paramName,
                         @Nullable String paramValue,
                         @Nullable String comment,
                         @Nullable String error,
                         @Nullable String notice,
                         int order) throws SQLException {

    String sql = """
      INSERT INTO {tableName} ({colFolder}, {colConfigName}, {colParamName}, {colParamValueStr}, {colComment}, {colError}, {colNotice}, {colOrder})
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      {onConflictDoNothing}
      """
      .replace("{onConflictDoNothing}", onConflictDoNothing())
      .replace("{tableName}", def.tableName)
      .replace("{colFolder}", def.colFolder)
      .replace("{colConfigName}", def.colConfigName)
      .replace("{colParamName}", def.colParamName)
      .replace("{colParamValueStr}", def.colParamValueStr)
      .replace("{colComment}", def.colComment)
      .replace("{colError}", def.colError)
      .replace("{colNotice}", def.colNotice)
      .replace("{colOrder}", def.colOrder);

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, folder);
      ps.setString(2, configName);
      ps.setString(3, paramName);
      ps.setString(4, paramValue);
      ps.setString(5, comment);
      ps.setString(6, error);
      ps.setString(7, notice);
      ps.setInt(8, order);
      return ps.executeUpdate() == 1;
    }
  }
}
