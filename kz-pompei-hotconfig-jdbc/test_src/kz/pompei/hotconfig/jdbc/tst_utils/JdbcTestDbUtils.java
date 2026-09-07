package kz.pompei.hotconfig.jdbc.tst_utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbcBuilder;
import kz.pompei.hotconfig.jdbc.ConnectionGet;
import kz.pompei.hotconfig.jdbc.DatabaseType;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public abstract class JdbcTestDbUtils extends JdbcTestParent {

  /**
   * Inserts row into table of config
   *
   * @param connectionGet connection source
   * @param builder       table config builder
   * @param folder        folder of config
   * @param configName    config name
   * @param paramName     param name
   * @param paramValue    param string value
   * @param comment       comment
   */
  protected void insertRow(@NonNull ConnectionGet connectionGet,
                           @NonNull ConfigTunnelJdbcBuilder builder,
                           @NonNull String folder,
                           @NonNull String configName,
                           @NonNull String paramName,
                           @NonNull String paramValue,
                           @Nullable String comment) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      int order = nextOrder(connection, builder, folder, configName);
      insertRow(connection, builder, folder, configName, paramName, paramValue, comment, order);
    } catch (SQLException e) {
      throw new RuntimeException("Y1z2A3b4C5 :: Could not insert configuration test row into table: " + builder.tableName(), e);
    }
  }

  protected void insertRow(@NonNull ConnectionGet connectionGet,
                           @NonNull ConfigTunnelJdbcBuilder builder,
                           @NonNull String folder,
                           @NonNull String configName,
                           @NonNull String paramName,
                           @NonNull String paramValue,
                           @Nullable String comment,
                           int order) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      insertRow(connection, builder, folder, configName, paramName, paramValue, comment, order);
    } catch (SQLException e) {
      throw new RuntimeException("Y1z2A3b4C5 :: Could not insert configuration test row into table: " + builder.tableName(), e);
    }
  }

  private void insertRow(@NonNull Connection connection,
                         @NonNull ConfigTunnelJdbcBuilder builder,
                         @NonNull String folder,
                         @NonNull String configName,
                         @NonNull String paramName,
                         @NonNull String paramValue,
                         @Nullable String comment,
                         int order) throws SQLException {

    String sql = """
      INSERT INTO {tableName} ({colFolder}, {colConfigName}, {colParamName}, {colParamValueStr}, {colComment}, {colError}, {colNotice}, {colOrder})
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      """
      .replace("{tableName}", builder.tableName())
      .replace("{colFolder}", builder.colFolder())
      .replace("{colConfigName}", builder.colConfigName())
      .replace("{colParamName}", builder.colParamName())
      .replace("{colParamValueStr}", builder.colParamValueStr())
      .replace("{colComment}", builder.colComment())
      .replace("{colError}", builder.colError())
      .replace("{colNotice}", builder.colNotice())
      .replace("{colOrder}", builder.colOrder());

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, folder);
      ps.setString(2, configName);
      ps.setString(3, paramName);
      ps.setString(4, paramValue);
      ps.setString(5, comment);
      ps.setString(6, null);
      ps.setString(7, null);
      ps.setInt(8, order);
      ps.executeUpdate();
    }
  }

  /**
   * Updates a row in the config table.
   *
   * @param connectionGet connection source
   * @param builder       table config builder
   * @param folder        folder of config
   * @param configName    config name
   * @param paramName     param name
   * @param paramValue    param string value
   * @param comment       comment
   */
  protected void updateRow(@NonNull ConnectionGet connectionGet,
                           @NonNull ConfigTunnelJdbcBuilder builder,
                           @NonNull String folder,
                           @NonNull String configName,
                           @NonNull String paramName,
                           @NonNull String paramValue,
                           @Nullable String comment) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        UPDATE {tableName}
        SET {colParamValueStr} = ?, {colComment} = ?
        WHERE {colFolder} = ? AND {colConfigName} = ? AND {colParamName} = ?
        """
        .replace("{tableName}", builder.tableName())
        .replace("{colParamValueStr}", builder.colParamValueStr())
        .replace("{colComment}", builder.colComment())
        .replace("{colFolder}", builder.colFolder())
        .replace("{colConfigName}", builder.colConfigName())
        .replace("{colParamName}", builder.colParamName());

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, paramValue);
        ps.setString(2, comment);
        ps.setString(3, folder);
        ps.setString(4, configName);
        ps.setString(5, paramName);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("D6e7F8g9H0 :: Could not update configuration test row in table: " + builder.tableName(), e);
    }
  }

  /**
   * Reads the last modification timestamp of one row of the config table.
   *
   * @param connectionGet connection source
   * @param builder       table config builder
   * @param folder        folder of config
   * @param configName    config name
   * @param paramName     param name - an empty string means the row of the whole config
   *
   * @return the timestamp in milliseconds, or {@code null} when there is no such row
   */
  protected @Nullable Long lastModified(@NonNull ConnectionGet connectionGet,
                                        @NonNull ConfigTunnelJdbcBuilder builder,
                                        @NonNull String folder,
                                        @NonNull String configName,
                                        @NonNull String paramName) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colLastModified}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ? AND {colParamName} = ?
        """
        .replace("{colLastModified}", builder.colLastModified())
        .replace("{tableName}", builder.tableName())
        .replace("{colFolder}", builder.colFolder())
        .replace("{colConfigName}", builder.colConfigName())
        .replace("{colParamName}", builder.colParamName());

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.setString(3, paramName);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return null;
          Timestamp timestamp = rs.getTimestamp(1);
          return timestamp == null ? null : timestamp.toInstant().toEpochMilli();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Rt5Yu8Iq3W :: Could not read last modification time from table: " + builder.tableName(), e);
    }
  }

  /**
   * Creates table for config
   *
   * @param connectionGet connection source
   * @param builder       table config builder
   */
  protected void createTable(@NonNull ConnectionGet connectionGet, @NonNull ConfigTunnelJdbcBuilder builder) {
    builder.connectionGet(connectionGet).build().createTableIfNotExists();
  }

  private int nextOrder(@NonNull Connection connection,
                        @NonNull ConfigTunnelJdbcBuilder builder,
                        @NonNull String folder,
                        @NonNull String configName) throws SQLException {
    String sql = """
      SELECT COALESCE(MAX({colOrder}), -1) + 1
      FROM {tableName}
      WHERE {colFolder} = ? AND {colConfigName} = ?
      """
      .replace("{colOrder}", builder.colOrder())
      .replace("{tableName}", builder.tableName())
      .replace("{colFolder}", builder.colFolder())
      .replace("{colConfigName}", builder.colConfigName());

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, folder);
      ps.setString(2, configName);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return 0;
        return rs.getInt(1);
      }
    }
  }

  protected boolean tableExists(@NonNull ConnectionGet connectionGet, @NonNull String tableName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
        while (rs.next()) {
          String name = rs.getString("TABLE_NAME");
          if (name != null && name.equalsIgnoreCase(tableName)) return true;
        }
      }
      return false;
    } catch (SQLException e) {
      throw new RuntimeException("I1j2K3l4M5 :: Could not inspect configuration test tables: " + tableName, e);
    }
  }

  /**
   * Checks that the table has a trigger with the specified name.
   *
   * @param connectionGet connection source
   * @param tableName     table name - without the schema part
   * @param triggerName   trigger name - without the schema part
   *
   * @return {@code true} when the trigger exists
   */
  protected boolean triggerExists(@NonNull ConnectionGet connectionGet, @NonNull String tableName, @NonNull String triggerName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = "SELECT trigger_name, event_object_table FROM information_schema.triggers";
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            String currentTriggerName = rs.getString("trigger_name");
            String currentTableName   = rs.getString("event_object_table");
            if (currentTriggerName != null
                && currentTableName != null
                && currentTriggerName.equalsIgnoreCase(triggerName)
                && currentTableName.equalsIgnoreCase(tableName)) {
              return true;
            }
          }
        }
      }
      return false;
    } catch (SQLException e) {
      throw new RuntimeException("Kw9Zx4Cv7B :: Could not inspect configuration test trigger: " + tableName + " / " + triggerName, e);
    }
  }

  /**
   * Drops the trigger of the config table, to imitate a table which exists without a trigger.
   *
   * @param connectionGet connection source
   * @param databaseType  database type - PostgreSQL needs the table in the DROP statement, MariaDB does not accept it
   * @param tableName     table name
   * @param triggerName   trigger name
   */
  protected void dropTrigger(@NonNull ConnectionGet connectionGet,
                             @NonNull DatabaseType databaseType,
                             @NonNull String tableName,
                             @NonNull String triggerName) {

    String sql = switch (databaseType) {
      case PostgreSQL -> "DROP TRIGGER IF EXISTS " + triggerName + " ON " + tableName;
      case MariaDB -> "DROP TRIGGER IF EXISTS " + triggerName;
    };

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Pm3Vb8Nx6L :: Could not drop configuration test trigger: " + tableName + " / " + triggerName, e);
    }
  }

  /**
   * Reads the identifier of the trigger inside PostgreSQL, to see whether the trigger is the same object as before.
   *
   * @param connectionGet connection source
   * @param tableName     table name - without the schema part
   * @param triggerName   trigger name
   *
   * @return the {@code pg_trigger.oid} of the trigger, or {@code null} when there is no such trigger
   */
  protected @Nullable Long triggerOidPg(@NonNull ConnectionGet connectionGet, @NonNull String tableName, @NonNull String triggerName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT t.oid
        FROM pg_trigger t
        JOIN pg_class c ON c.oid = t.tgrelid
        WHERE LOWER(t.tgname) = LOWER(?) AND LOWER(c.relname) = LOWER(?)
        """;
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, triggerName);
        ps.setString(2, tableName);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return null;
          return rs.getLong(1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Zq7Hd2Tf5R :: Could not read configuration test trigger oid: " + tableName + " / " + triggerName, e);
    }
  }

  /**
   * Runs the specified SQL statement, to bring the database into a state which the tunnel must survive.
   *
   * @param connectionGet connection source
   * @param sql           SQL statement
   */
  protected void execSql(@NonNull ConnectionGet connectionGet, @NonNull String sql) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        statement.execute(sql);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Xt2Wq9Ly4N :: Could not run configuration test statement: " + sql, e);
    }
  }

  /**
   * Reads the body of the trigger of the config table - the text after {@code FOR EACH ROW}.
   *
   * @param connectionGet connection source
   * @param tableName     table name - without the schema part
   * @param triggerName   trigger name
   *
   * @return the body of the trigger, or {@code null} when there is no such trigger
   */
  protected @Nullable String triggerBody(@NonNull ConnectionGet connectionGet, @NonNull String tableName, @NonNull String triggerName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT action_statement
        FROM information_schema.triggers
        WHERE LOWER(trigger_name) = LOWER(?) AND LOWER(event_object_table) = LOWER(?)
        """;
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, triggerName);
        ps.setString(2, tableName);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return null;
          return rs.getString(1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Cf8Rn5Mj2V :: Could not read configuration test trigger body: " + tableName + " / " + triggerName, e);
    }
  }

  protected boolean columnExists(@NonNull ConnectionGet connectionGet, @NonNull String tableName, @NonNull String columnName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      try (ResultSet rs = metaData.getColumns(null, null, "%", "%")) {
        while (rs.next()) {
          String currentTableName = rs.getString("TABLE_NAME");
          String currentColumnName = rs.getString("COLUMN_NAME");
          if (currentTableName != null
              && currentColumnName != null
              && currentTableName.equalsIgnoreCase(tableName)
              && currentColumnName.equalsIgnoreCase(columnName)) {
            return true;
          }
        }
      }
      return false;
    } catch (SQLException e) {
      throw new RuntimeException("Ja7Kp2Qr4S :: Could not inspect configuration test column: " + tableName + "." + columnName, e);
    }
  }

  @SuppressWarnings("SqlWithoutWhere")
  protected void clearTable(@NonNull ConnectionGet connectionGet, @NonNull String tableName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + tableName)) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("N2o3P4q5R6 :: Could not clear configuration test table: " + tableName, e);
    }
  }

  protected static void waitForChange(@NonNull DatabaseType databaseType) throws InterruptedException {
    Thread.sleep(databaseType == DatabaseType.MariaDB ? 1200 : 100);
  }
}
