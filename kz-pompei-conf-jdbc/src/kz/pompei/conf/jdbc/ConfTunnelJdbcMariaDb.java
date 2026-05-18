package kz.pompei.conf.jdbc;

import lombok.NonNull;

public class ConfTunnelJdbcMariaDb extends ConfTunnelJdbc {
  public ConfTunnelJdbcMariaDb(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    super(connectionGet, params);
  }
}
