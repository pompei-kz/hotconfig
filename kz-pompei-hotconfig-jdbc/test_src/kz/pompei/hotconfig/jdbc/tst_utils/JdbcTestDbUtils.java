package kz.pompei.hotconfig.jdbc.tst_utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
