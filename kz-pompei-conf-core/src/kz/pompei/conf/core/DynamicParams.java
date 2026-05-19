package kz.pompei.conf.core;

import lombok.NonNull;

/**
 * Interface for providing dynamic parameters for a hot config system.
 */
public interface DynamicParams {

  /**
   * Returns the current time in milliseconds.
   *
   * @return current time in milliseconds
   */
  long now();

  /**
   * Retrieves the value of an environment variable by name.
   *
   * @param envName the name of the environment variable
   * @return the value of the environment variable, or null if not found
   */
  String env(@NonNull String envName);

  DynamicParams REAL = new DynamicParams() {
    @Override public long now() {
      return System.currentTimeMillis();
    }

    @Override public String env(@NonNull String envName) {
      return System.getenv(envName);
    }
  };
}
