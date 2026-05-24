package kz.pompei.hotconfig.core;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for accessing environment variables.
 */
public interface EnvSrc {

  /**
   * Retrieves the value of an environment variable by name.
   *
   * @param envName the name of the environment variable
   * @return the value of the environment variable, or null if not found
   */
  @Nullable String env(@NonNull String envName);

  EnvSrc REAL = System::getenv;
}
