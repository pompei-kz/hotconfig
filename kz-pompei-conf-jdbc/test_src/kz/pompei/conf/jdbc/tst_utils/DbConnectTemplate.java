package kz.pompei.conf.jdbc.tst_utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbConnectTemplate {
  public final String urlTemplate;
  public final String username;
  public final String password;
}
