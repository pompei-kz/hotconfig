package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import kz.pompei.hotconfig.core.ConfigTunnel;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import lombok.NonNull;
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

  @NonNull protected final ConfigTunnelJdbcDef params;
  @NonNull protected final ConnectionGet       connectionGet;

  protected ConfigTunnelJdbc(@NonNull ConnectionGet connectionGet, @NonNull ConfigTunnelJdbcDef params) {
    this.params        = params;
    this.connectionGet = connectionGet;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT {colParamName}, {colParamValueStr}, {colComment}
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        ORDER BY {colParamName}
        """
        .replace("{colParamName}", params.colParamName)
        .replace("{colParamValueStr}", params.colParamValueStr)
        .replace("{colComment}", params.colComment)
        .replace("{tableName}", params.tableName)
        .replace("{colFolder}", params.colFolder)
        .replace("{colConfigName}", params.colConfigName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);

        try (ResultSet rs = ps.executeQuery()) {
          Conf    conf = new Conf();
          boolean ok   = false;
          while (rs.next()) {
            ok = true;
            String paramName = rs.getString(params.colParamName);
            String comment   = rs.getString(params.colComment);
            if (paramName == null || paramName.isEmpty()) {
              conf.confComments.addAll(commentLines(comment));
            } else {
              ConfParam param = new ConfParam();
              param.name     = paramName;
              param.valueStr = rs.getString(params.colParamValueStr);
              param.comments.addAll(commentLines(comment));
              conf.params.add(param);
            }
          }
          return ok ? conf : null;
        }
      }
    } catch (SQLException e) {
      if (isMissingTable(e)) return null;
      throw new RuntimeException("Z6a7B8c9D0 :: Could not read configuration from table: " + params.tableName, e);
    }
  }

  @Override public void write(@NonNull String localPath, @NonNull Conf conf) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    createTableIfNotExists();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        DELETE FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        """
        .replace("{tableName}", params.tableName)
        .replace("{colFolder}", params.colFolder)
        .replace("{colConfigName}", params.colConfigName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.executeUpdate();
      }

      insertRow(connection, folder, configName, "", "", String.join("\n", conf.confComments), null);
      for (ConfParam param : conf.params) {
        insertRow(connection, folder, configName, param.name, param.valueStr, String.join("\n", param.comments), null);
      }
    } catch (SQLException e) {
      throw new RuntimeException("E1f2G3h4I5 :: Could not write configuration to table: " + params.tableName, e);
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
        .replace("{colNotice}", params.colNotice)
        .replace("{tableName}", params.tableName)
        .replace("{colFolder}", params.colFolder)
        .replace("{colConfigName}", params.colConfigName)
        .replace("{colParamName}", params.colParamName);

      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.setString(3, "");
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return List.of();
          return lines(rs.getString(params.colNotice));
        }
      }
    } catch (SQLException e) {
      if (isMissingTable(e)) return List.of();
      throw new RuntimeException("V1w2X3y4Z5 :: Could not read configuration notice from table: " + params.tableName, e);
    }
  }

  @Override public void writeNoticeLines(@NonNull String localPath, @NonNull List<String> lines) {
    String folder     = folder(localPath);
    String configName = configName(localPath);
    String notice     = String.join("\n", lines);

    createTableIfNotExists();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String updateSql = """
        UPDATE {tableName}
        SET {colNotice} = ?
        WHERE {colFolder} = ? AND {colConfigName} = ? AND {colParamName} = ?
        """
        .replace("{tableName}", params.tableName)
        .replace("{colNotice}", params.colNotice)
        .replace("{colFolder}", params.colFolder)
        .replace("{colConfigName}", params.colConfigName)
        .replace("{colParamName}", params.colParamName);

      try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
        ps.setString(1, notice);
        ps.setString(2, folder);
        ps.setString(3, configName);
        ps.setString(4, "");
        if (ps.executeUpdate() > 0) return;
      }

      insertRow(connection, folder, configName, "", null, null, notice);
    } catch (SQLException e) {
      throw new RuntimeException("A6b7C8d9E0 :: Could not write configuration notice to table: " + params.tableName, e);
    }
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    createTableIfNotExists();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String sql = """
        SELECT MAX({colLastModified})
        FROM {tableName}
        WHERE {colFolder} = ? AND {colConfigName} = ?
        """
        .replace("{colLastModified}", params.colLastModified)
        .replace("{tableName}", params.tableName)
        .replace("{colFolder}", params.colFolder)
        .replace("{colConfigName}", params.colConfigName);

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
      throw new RuntimeException("J6k7L8m9N0 :: Could not get configuration modification time from table: " + params.tableName, e);
    }
  }

  public abstract void createTableIfNotExists();

  protected void createSchemaIfNotExists(@NonNull Connection connection) throws SQLException {
    int dotIndex = params.tableName.indexOf('.');
    if (dotIndex < 0) return;

    String schema = params.tableName.substring(0, dotIndex);
    try (PreparedStatement ps = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + schema)) {
      ps.executeUpdate();
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
      String sqlState = current.getSQLState();
      int errorCode = current.getErrorCode();
      String message = current.getMessage();
      if ("42P01".equals(sqlState) || "42S02".equals(sqlState) || errorCode == 1146) return true;
      if (message != null && (
        message.contains("does not exist")
          || message.contains("Unknown table")
          || message.contains("Base table or view not found")
      )) return true;
      current = current.getNextException();
    }
    return false;
  }

  private void insertRow(@NonNull Connection connection,
                         @NonNull String folder,
                         @NonNull String configName,
                         @NonNull String paramName,
                         @Nullable String paramValue,
                         @Nullable String comment,
                         @Nullable String notice) throws SQLException {

    String sql = """
      INSERT INTO {tableName} ({colFolder}, {colConfigName}, {colParamName}, {colParamValueStr}, {colComment}, {colNotice})
      VALUES (?, ?, ?, ?, ?, ?)
      """
      .replace("{tableName}", params.tableName)
      .replace("{colFolder}", params.colFolder)
      .replace("{colConfigName}", params.colConfigName)
      .replace("{colParamName}", params.colParamName)
      .replace("{colParamValueStr}", params.colParamValueStr)
      .replace("{colComment}", params.colComment)
      .replace("{colNotice}", params.colNotice);

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, folder);
      ps.setString(2, configName);
      ps.setString(3, paramName);
      ps.setString(4, paramValue);
      ps.setString(5, comment);
      ps.setString(6, notice);
      ps.executeUpdate();
    }
  }
}
