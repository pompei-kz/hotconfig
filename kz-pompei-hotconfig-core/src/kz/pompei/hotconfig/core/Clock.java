package kz.pompei.hotconfig.core;

/**
 * Clock interface provides a method to retrieve the current time in milliseconds.
 */
public interface Clock {

  /**
   * Returns the current time in milliseconds.
   *
   * @return current time in milliseconds
   */
  long nowMs();

  Clock REAL = System::currentTimeMillis;
}
