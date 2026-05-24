package app;

import kz.pompei.hotconfig.core.ann.ConfDefaultValue;
import kz.pompei.hotconfig.core.ann.ConfDoc;

@ConfDoc("Access to database")
public interface DbAccessConfig {

  @ConfDoc("host")
  @ConfDefaultValue("localhost")
  String host();

  @ConfDoc("port")
  @ConfDefaultValue("fff1000")
  int port();

  @ConfDoc("username")
  @ConfDefaultValue("some user")
  String username();

  @ConfDoc("password")
  @ConfDefaultValue("super secret")
  String password();
}
