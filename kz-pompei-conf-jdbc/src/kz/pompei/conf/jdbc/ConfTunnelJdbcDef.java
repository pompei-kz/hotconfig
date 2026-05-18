package kz.pompei.conf.jdbc;

/**
 * Contains the data necessary to determine in which table the configuration should be stored,
 * and in which fields of this table what should be stored
 */
public class ConfTunnelJdbcDef {

  /**
   * The name of the column in which the directory where the config is stored.
   * <p>
   * If the config is located at the root, then this field should be equal to an empty string.
   */
  public String colFolder = "folder";

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
  public String tableName = "conf";


  /**
   * The name of the column in which the config name is stored.
   */
  public String colConfigName = "class_name";

  /**
   * Parameter name.
   * <p>
   * If the parameter name is an empty string, this means that this table row applies to the config itself.
   * This is important for the field containing the comment—it applies to the entire config.
   * <p>
   * If this field is empty, it means the table row belongs to a configuration file. If it is filled in, it belongs to a parameter.
   */
  public String colParamName = "name";

  /**
   * String value of the parameter
   */
  public String colParamValueStr = "value_str";

  /**
   * The name of the column containing the parameter comment text.
   * If the parameter name is undefined in this line, this comment applies to the config itself.
   * <p>
   * The comment is stored as a single text string—lines are joined by a line break (\n) when writing.
   */
  public String colComment = "cmt";

  /**
   * The name of the column containing the creation timestamp of the config or parameter.
   */
  public String colCreatedAt = "created_at";

  /**
   * The name of the column containing the last modification timestamp of the config parameter.
   * <p>
   * This column is updated whenever any field in this row is updated.
   */
  public String colLastModified = "last_modified_at";
}
