package kz.pompei.conf.jdbc.tst_utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import kz.pompei.conf.jdbc.ConfTunnelJdbcBuilder;
import kz.pompei.conf.jdbc.ConfTunnelJdbcDef;
import kz.pompei.conf.jdbc.ConnectionGet;
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
                           @NonNull ConfTunnelJdbcDef def,
                           @NonNull String folder,
                           @NonNull String configName,
                           @NonNull String paramName,
                           @NonNull String paramValue,
                           @Nullable String comment) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("""
        INSERT INTO %s (%s, %s, %s, %s, %s)
        VALUES (?, ?, ?, ?, ?)
        """.formatted(
        def.tableName,
        def.colFolder,
        def.colConfigName,
        def.colParamName,
        def.colParamValueStr,
        def.colComment
      ))) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.setString(3, paramName);
        ps.setString(4, paramValue);
        ps.setString(5, comment);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("4DgS0NB9DX :: Could not insert configuration test row into table: " + def.tableName, e);
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
                           @NonNull ConfTunnelJdbcDef def,
                           @NonNull String folder,
                           @NonNull String configName,
                           @NonNull String paramName,
                           @NonNull String paramValue,
                           @Nullable String comment) {

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("""
        UPDATE %s
        SET %s = ?, %s = ?
        WHERE %s = ? AND %s = ? AND %s = ?
        """.formatted(
        def.tableName,
        def.colParamValueStr,
        def.colComment,
        def.colFolder,
        def.colConfigName,
        def.colParamName
      ))) {
        ps.setString(1, paramValue);
        ps.setString(2, comment);
        ps.setString(3, folder);
        ps.setString(4, configName);
        ps.setString(5, paramName);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("7bJ5mD8hQ2 :: Could not update configuration test row in table: " + def.tableName, e);
    }
  }

  /**
   * Creates table for config
   *
   * @param connectionGet connection source
   * @param def           table config definition
   */
  protected void createTable(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef def) {
    ConfTunnelJdbcBuilder.build(connectionGet, def).createTableIfNotExists();
  }

  @SuppressWarnings("SqlWithoutWhere")
  protected void clearTable(@NonNull ConnectionGet connectionGet, @NonNull String tableName) {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + tableName)) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("r5Tg8Yh2Kp :: Could not clear configuration test table: " + tableName, e);
    }
  }
}
