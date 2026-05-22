package kz.pompei.hotconfig.jdbc.tst_utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbcBuilder;
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbcDef;
import kz.pompei.hotconfig.jdbc.ConnectionGet;
import kz.pompei.hotconfig.jdbc.DatabaseType;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public abstract class JdbcTestDbUtils extends JdbcTestParent {

  /**
   * Inserts row into table of config
   *
   * @param connectionGet connection source
   * @param def           table config definition
   * @param folder        folder of config
   * @param configName    config name
   * @param paramName     param name
   * @param paramValue    param string value
   * @param comment       comment
   */
  protected void insertRow(@NonNull ConnectionGet connectionGet,
                           @NonNull ConfigTunnelJdbcDef def,
                           @NonNull String folder,
                           @NonNull String configName,
                           @NonNull String paramName,
                           @NonNull String paramValue,
                           @Nullable String comment) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        INSERT INTO {tableName} ({colFolder}, {colConfigName}, {colParamName}, {colParamValueStr}, {colComment}, {colNotice})
        VALUES (?, ?, ?, ?, ?, ?)
        """
        .replace("{tableName}", def.tableName)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName)
        .replace("{colParamName}", def.colParamName)
        .replace("{colParamValueStr}", def.colParamValueStr)
        .replace("{colComment}", def.colComment)
        .replace("{colNotice}", def.colNotice);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.setString(3, paramName);
        ps.setString(4, paramValue);
        ps.setString(5, comment);
        ps.setString(6, null);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Y1z2A3b4C5 :: Could not insert configuration test row into table: " + def.tableName, e);
    }
  }

  /**
   * Updates a row in the config table.
   *
   * @param connectionGet connection source
   * @param def           table config definition
   * @param folder        folder of config
   * @param configName    config name
   * @param paramName     param name
   * @param paramValue    param string value
   * @param comment       comment
   */
  protected void updateRow(@NonNull ConnectionGet connectionGet,
                           @NonNull ConfigTunnelJdbcDef def,
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
        .replace("{tableName}", def.tableName)
        .replace("{colParamValueStr}", def.colParamValueStr)
        .replace("{colComment}", def.colComment)
        .replace("{colFolder}", def.colFolder)
        .replace("{colConfigName}", def.colConfigName)
        .replace("{colParamName}", def.colParamName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, paramValue);
        ps.setString(2, comment);
        ps.setString(3, folder);
        ps.setString(4, configName);
        ps.setString(5, paramName);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("D6e7F8g9H0 :: Could not update configuration test row in table: " + def.tableName, e);
    }
  }

  /**
   * Creates table for config
   *
   * @param connectionGet connection source
   * @param def           table config definition
   */
  protected void createTable(@NonNull ConnectionGet connectionGet, @NonNull ConfigTunnelJdbcDef def) {
    ConfigTunnelJdbcBuilder.build(connectionGet, def).createTableIfNotExists();
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
