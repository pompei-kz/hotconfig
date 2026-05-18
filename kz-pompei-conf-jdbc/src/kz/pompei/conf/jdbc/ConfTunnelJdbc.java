package kz.pompei.conf.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import kz.pompei.conf.core.ConfTunnel;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
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
public abstract class ConfTunnelJdbc implements ConfTunnel {

  @NonNull protected final ConfTunnelJdbcDef params;
  @NonNull protected final ConnectionGet     connectionGet;

  protected ConfTunnelJdbc(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    this.params        = params;
    this.connectionGet = connectionGet;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    createTableIfNotExists();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("""
        SELECT %s, %s, %s
        FROM %s
        WHERE %s = ? AND %s = ?
        ORDER BY %s
        """.formatted(
        params.colParamName,
        params.colParamValueStr,
        params.colComment,
        params.tableName,
        params.colFolder,
        params.colConfigName,
        params.colParamName
      ))) {
        ps.setString(1, folder);
        ps.setString(2, configName);

        try (ResultSet rs = ps.executeQuery()) {
          Conf conf  = new Conf();
          boolean ok = false;
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
      throw new RuntimeException("K6fR8pQ2mN :: Could not read configuration from table: " + params.tableName, e);
    }
  }

  @Override public void write(@NonNull String confPath, @NonNull Conf conf) {
    String folder     = folder(confPath);
    String configName = configName(confPath);

    createTableIfNotExists();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("""
        DELETE FROM %s
        WHERE %s = ? AND %s = ?
        """.formatted(
        params.tableName,
        params.colFolder,
        params.colConfigName
      ))) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        ps.executeUpdate();
      }

      insertRow(connection, folder, configName, "", "", String.join("\n", conf.confComments));
      for (ConfParam param : conf.params) {
        insertRow(connection, folder, configName, param.name, param.valueStr, String.join("\n", param.comments));
      }
    } catch (SQLException e) {
      throw new RuntimeException("p2Lk8Mn4Qs :: Could not write configuration to table: " + params.tableName, e);
    }
  }

  @Override public @Nullable Instant lastModified(@NonNull String localPath) {
    String folder     = folder(localPath);
    String configName = configName(localPath);

    createTableIfNotExists();

    try (@NonNull Connection connection = connectionGet.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement("""
        SELECT MAX(%s)
        FROM %s
        WHERE %s = ? AND %s = ?
        """.formatted(
        params.colLastModified,
        params.tableName,
        params.colFolder,
        params.colConfigName
      ))) {
        ps.setString(1, folder);
        ps.setString(2, configName);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) return null;
          Timestamp timestamp = rs.getTimestamp(1);
          if (timestamp == null) return null;
          return timestamp.toInstant();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Q9rT6vB3nM :: Could not get configuration modification time from table: " + params.tableName, e);
    }
  }

  protected void createTableIfNotExists() {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      createSchemaIfNotExists(connection);
      try (PreparedStatement ps = connection.prepareStatement("""
        CREATE TABLE IF NOT EXISTS %s (
          %s VARCHAR(1000) NOT NULL,
          %s VARCHAR(1000) NOT NULL,
          %s VARCHAR(1000) NOT NULL,
          %s TEXT,
          %s TEXT,
          %s TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          %s TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """.formatted(
        params.tableName,
        params.colFolder,
        params.colConfigName,
        params.colParamName,
        params.colParamValueStr,
        params.colComment,
        params.colCreatedAt,
        params.colLastModified
      ))) {
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("W3tY7uI9oP :: Could not create configuration table: " + params.tableName, e);
    }
  }

  private void createSchemaIfNotExists(@NonNull Connection connection) throws SQLException {
    int dotIndex = params.tableName.indexOf('.');
    if (dotIndex < 0) return;

    String schema = params.tableName.substring(0, dotIndex);
    try (PreparedStatement ps = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + schema)) {
      ps.executeUpdate();
    }
  }

  private String folder(@NonNull String localPath) {
    int slashIndex = localPath.lastIndexOf('/');
    if (slashIndex < 0) return "";
    return localPath.substring(0, slashIndex);
  }

  private String configName(@NonNull String localPath) {
    int slashIndex = localPath.lastIndexOf('/');
    if (slashIndex < 0) return localPath;
    return localPath.substring(slashIndex + 1);
  }

  private List<String> commentLines(@Nullable String comment) {
    if (comment == null || comment.isEmpty()) return List.of();
    return Arrays.asList(comment.split("\\R", -1));
  }

  private void insertRow(@NonNull Connection connection,
                         @NonNull String folder,
                         @NonNull String configName,
                         @NonNull String paramName,
                         @Nullable String paramValue,
                         @Nullable String comment) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("""
      INSERT INTO %s (%s, %s, %s, %s, %s)
      VALUES (?, ?, ?, ?, ?)
      """.formatted(
      params.tableName,
      params.colFolder,
      params.colConfigName,
      params.colParamName,
      params.colParamValueStr,
      params.colComment
    ))) {
      ps.setString(1, folder);
      ps.setString(2, configName);
      ps.setString(3, paramName);
      ps.setString(4, paramValue);
      ps.setString(5, comment);
      ps.executeUpdate();
    }
  }
}
