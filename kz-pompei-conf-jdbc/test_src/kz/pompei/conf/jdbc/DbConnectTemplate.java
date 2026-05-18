package kz.pompei.conf.jdbc;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbConnectTemplate {
  public final String urlTemplate;
  public final String username;
  public final String password;
}
