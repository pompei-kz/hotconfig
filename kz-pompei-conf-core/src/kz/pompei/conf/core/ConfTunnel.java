package kz.pompei.conf.core;

import kz.pompei.conf.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Storage adapter for reading and writing configuration data.
 * <p>
 * Implementations decide where and how the configuration is stored. Callers address a configuration by a local path relative to the
 * implementation's storage root.
 */
public interface ConfTunnel {

  /**
   * Reads configuration data from the storage.
   *
   * @param localPath path of the configuration relative to the storage root
   * @return parsed configuration, or {@code null} when there is no configuration at {@code localPath}
   */
  @Nullable Conf read(@NonNull String localPath);

  /**
   * Writes configuration data to the storage.
   *
   * @param confPath path of the configuration relative to the storage root
   * @param conf     configuration data to write
   */
  void write(@NonNull String confPath, @NonNull Conf conf);

  /**
   * Returns the last modification time for stored configuration data in epoch milliseconds.
   *
   * @param localPath path of the configuration relative to the storage root
   * @return last modification time in milliseconds, or {@code null} when there is no configuration at {@code localPath}
   */
  @Nullable Long modificationMarker(@NonNull String localPath);

}
