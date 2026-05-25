package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.NonNull;

/**
 * Builder for {@link ConfigTunnelJdbc}.
 * <p>
 * The resulting tunnel stores configuration rows in a JDBC database table and selects the concrete implementation from JDBC metadata.
 */
public class ConfigTunnelJdbcBuilder {

  private ConnectionGet connectionGet;

  /**
   * Source of JDBC connections used for all tunnel reads and writes.
   */
  public @NonNull ConfigTunnelJdbcBuilder connectionGet(@NonNull ConnectionGet connectionGet) {
    this.connectionGet = connectionGet;
    return this;
  }

  /**
   * The name of the column in which the directory where the config is stored.
   * <p>
   * If the config is located at the root, then this field should be equal to an empty string.
   */
  private String colFolder = "folder";

  /**
   * Table name for configuration storage in the database.
   * <p>
   * If such a table does not exist in the database, it is created automatically.
   * <p>
   * The name may contain a period (.) - in this case, before the period is the schema name,
   * and after the period is the name of a table in that schema.
   * <p>
   * In this case, both the table and the diagram are created automatically if one of them is missing.
   * <p>
   * This field can never be empty.
   */
  private String tableName = "conf";


  /**
   * The name of the column in which the config name is stored.
   */
  private String colConfigName = "class_name";

  /**
   * Parameter name.
   * <p>
   * If the parameter name is an empty string, this means that this table row applies to the config itself.
   * This is important for the field containing the comment—it applies to the entire config.
   * <p>
   * If this field is empty, it means the table row belongs to a configuration file. If it is filled in, it belongs to a parameter.
   */
  private String colParamName = "name";

  /**
   * String value of the parameter
   */
  private String colParamValueStr = "value_str";

  /**
   * The name of the column containing the parameter comment text.
   * If the parameter name is undefined in this line, this comment applies to the config itself.
   * <p>
   * The comment is stored as a single text string—lines are joined by a line break (\n) when writing.
   */
  private String colComment = "cmt";

  /**
   * The name of the column containing parameter error text.
   * <p>
   * This field is used only for parameter rows; the config row leaves it null.
   */
  private String colError = "error";

  /**
   * The name of the column containing notice text associated with the whole config.
   * <p>
   * The notice is stored as a single text string—lines are joined by a line break (\n) when writing.
   * This field is used only for the config row; parameter rows leave it null.
   */
  private String colNotice = "notice";

  /**
   * The name of the column containing the creation timestamp of the config or parameter.
   */
  private String colCreatedAt = "created_at";

  /**
   * The name of the column containing the last modification timestamp of the config parameter.
   * <p>
   * This column is updated whenever any field in this row is updated.
   */
  private String colLastModified = "last_modified_at";

  /**
   * Column containing configuration-level or parameter-level comments.
   */
  public @NonNull ConfigTunnelJdbcBuilder colComment(@NonNull String colComment) {
    this.colComment = colComment;
    return this;
  }

  /**
   * Column containing the configuration file name.
   */
  public @NonNull ConfigTunnelJdbcBuilder colConfigName(@NonNull String colConfigName) {
    this.colConfigName = colConfigName;
    return this;
  }

  /**
   * Column containing row creation timestamps.
   */
  public @NonNull ConfigTunnelJdbcBuilder colCreatedAt(@NonNull String colCreatedAt) {
    this.colCreatedAt = colCreatedAt;
    return this;
  }

  /**
   * Column containing parameter error text.
   */
  public @NonNull ConfigTunnelJdbcBuilder colError(@NonNull String colError) {
    this.colError = colError;
    return this;
  }


  /**
   * Column containing the configuration folder path.
   */
  public @NonNull ConfigTunnelJdbcBuilder colFolder(@NonNull String colFolder) {
    this.colFolder = colFolder;
    return this;
  }


  /**
   * Column containing row last-modified timestamps.
   */
  public @NonNull ConfigTunnelJdbcBuilder colLastModified(@NonNull String colLastModified) {
    this.colLastModified = colLastModified;
    return this;
  }


  /**
   * Column containing configuration-level notice text.
   */
  public @NonNull ConfigTunnelJdbcBuilder colNotice(@NonNull String colNotice) {
    this.colNotice = colNotice;
    return this;
  }


  /**
   * Column containing parameter names.
   */
  public @NonNull ConfigTunnelJdbcBuilder colParamName(@NonNull String colParamName) {
    this.colParamName = colParamName;
    return this;
  }


  /**
   * Column containing parameter string values.
   */
  public @NonNull ConfigTunnelJdbcBuilder colParamValueStr(@NonNull String colParamValueStr) {
    this.colParamValueStr = colParamValueStr;
    return this;
  }


  /**
   * Table name for configuration storage.
   */
  public @NonNull ConfigTunnelJdbcBuilder tableName(@NonNull String tableName) {
    this.tableName = tableName;
    return this;
  }

  public @NonNull String colComment() {
    return colComment;
  }

  public @NonNull String colConfigName() {
    return colConfigName;
  }

  public @NonNull String colCreatedAt() {
    return colCreatedAt;
  }

  public @NonNull String colError() {
    return colError;
  }

  public @NonNull String colFolder() {
    return colFolder;
  }

  public @NonNull String colLastModified() {
    return colLastModified;
  }

  public @NonNull String colNotice() {
    return colNotice;
  }

  public @NonNull String colParamName() {
    return colParamName;
  }

  public @NonNull String colParamValueStr() {
    return colParamValueStr;
  }

  public @NonNull String tableName() {
    return tableName;
  }

  public @NonNull ConfigTunnelJdbc build() {

    if (connectionGet == null) {
      throw new IllegalArgumentException("u5W8jIn1ZH :: ConnectionGet cannot be null");
    }

    ConfigTunnelJdbc.Def def = new ConfigTunnelJdbc.Def(colFolder,
                                                        tableName,
                                                        colConfigName,
                                                        colParamName,
                                                        colParamValueStr,
                                                        colComment,
                                                        colError,
                                                        colNotice,
                                                        colCreatedAt,
                                                        colLastModified);

    DatabaseType databaseType = detectDb(connectionGet);

    return switch (databaseType) {
      case PostgreSQL -> new ConfigTunnelJdbcPg(connectionGet, def);
      case MariaDB -> new ConfigTunnelJdbcMariaDb(connectionGet, def);
    };
  }

  private static @NonNull DatabaseType detectDb(@NonNull ConnectionGet connectionGet) throws UnknownDb {
    try (@NonNull Connection connection = connectionGet.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      if (productName.contains("PostgreSQL")) return DatabaseType.PostgreSQL;
      if (productName.contains("MariaDB")) return DatabaseType.MariaDB;
      throw new UnknownDb("Foh9XKIbpC :: productName = " + productName);
    } catch (SQLException e) {
      throw new RuntimeException("U1v2W3x4Y5 :: Could not detect database type", e);
    }
  }
}
