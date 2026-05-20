package kz.pompei.hotconfig.jdbc;

import java.sql.Connection;
import lombok.NonNull;

public interface ConnectionGet {
  @NonNull Connection getConnection();
}
