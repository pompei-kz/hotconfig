package kz.pompei.conf.jdbc;

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

    throw new RuntimeException("4DgS0NB9DX :: Not implemented yet");
  }

  /**
   * Creates table for config
   *
   * @param connectionGet connection source
   * @param def           table config definition
   */
  protected void insertCreatesTable(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef def) {
    throw new RuntimeException("2026-05-18 17:29 Created empty method body ConfTunnelJdbcTest.insertCreatesTable()");
  }
}
