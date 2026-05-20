package kz.pompei.hotconfig.jdbc.tst_utils;

import java.sql.Connection;
import java.sql.DriverManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DbConnectBuilder {
  public final String url;
  public final String username;
  public final String password;

  @SneakyThrows @NonNull Connection build() {
    return DriverManager.getConnection(url, username, password);
  }
}
