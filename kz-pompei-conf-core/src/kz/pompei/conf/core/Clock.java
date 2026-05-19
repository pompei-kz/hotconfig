package kz.pompei.conf.core;

/**
 * Interface for providing current time in milliseconds.
 */
public interface Clock {

  /**
   * Returns the current time in milliseconds.
   *
   * @return current time in milliseconds
   */
  long now();

  Clock REAL = System::currentTimeMillis;
}
