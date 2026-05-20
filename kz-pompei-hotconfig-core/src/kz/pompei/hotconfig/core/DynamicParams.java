package kz.pompei.hotconfig.core;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

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
  @Nullable String env(@NonNull String envName);

  DynamicParams REAL = new DynamicParams() {
    @Override public long now() {
      return System.currentTimeMillis();
    }

    @Override public @Nullable String env(@NonNull String envName) {
      return System.getenv(envName);
    }
  };
}
