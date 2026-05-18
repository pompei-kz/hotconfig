package kz.pompei.conf.jdbc;

import lombok.NonNull;

public class ConfTunnelJdbcPg extends ConfTunnelJdbc {
  public ConfTunnelJdbcPg(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    super(connectionGet, params);
  }
}
