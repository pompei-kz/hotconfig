package kz.pompei.hotconfig.jdbc;

public class UnknownDb extends RuntimeException {
  public UnknownDb(String message) {
    super(message);
  }
}
