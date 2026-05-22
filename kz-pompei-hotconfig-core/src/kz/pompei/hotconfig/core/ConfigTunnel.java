package kz.pompei.hotconfig.core;

import java.util.List;
import kz.pompei.hotconfig.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Storage adapter for reading and writing configuration data.
 * <p>
 * Implementations decide where and how the configuration is stored. Callers address a configuration by a local path relative to the
 * implementation's storage root.
 */
public interface ConfigTunnel {

  /**
   * Reads configuration data from the storage.
   *
   * @param localPath path of the configuration relative to the storage root
   * @return parsed configuration, or {@code null} when there is no configuration at {@code localPath}
   */
  @Nullable Conf read(@NonNull String localPath);

  /**
   * Reads lines of notice text associated with config by localPath
   *
   * @param localPath path of the configuration relative to the storage root
   * @return list of lines
   */
  @NonNull List<String> readNoticeLines(@NonNull String localPath);

  /**
   * Writes configuration data to the storage.
   *
   * @param localPath path of the configuration relative to the storage root
   * @param conf      configuration data to write
   */
  void write(@NonNull String localPath, @NonNull Conf conf);

  /**
   * Writes lines of notice text to config associated with config in path localPath
   *
   * @param localPath path of the configuration relative to the storage root
   * @param lines     list of lines to write
   */
  void writeNoticeLines(@NonNull String localPath, @NonNull List<String> lines);

  /**
   * Returns a modification marker for stored configuration data.
   * <p>
   * The marker is an implementation detail chosen so callers can quickly detect whether a configuration changed without rereading
   * the full content. It may be a modification time, a revision number, or some other value that changes when the configuration
   * changes.
   *
   * @param localPath path of the configuration relative to the storage root
   * @return modification marker, or {@code null} when there is no configuration at {@code localPath}
   */
  @Nullable Long modificationMarker(@NonNull String localPath);

}
