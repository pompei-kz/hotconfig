package kz.pompei.hotconfig.core;

import java.nio.file.Path;
import kz.pompei.hotconfig.core.ann.ConfFolder;
import lombok.NonNull;

/**
 * Builder for {@link ConfigTunnelFile}.
 * <p>
 * The resulting tunnel stores configuration files below a configured base directory and stores notice files by appending a configured
 * notice extension to the same local path.
 */
public class ConfigTunnelFileBuilder {

  private          Path   baseDir;
  private @NonNull String noticeExtension = ".notice";

  /**
   * The base directory in which files will be created. Paths are specified relative to this directory in the {@link ConfFolder} annotation.
   *
   * @param baseDir The base directory
   * @return this
   */
  public @NonNull ConfigTunnelFileBuilder baseDir(@NonNull Path baseDir) {
    this.baseDir = baseDir;
    return this;
  }

  /**
   * The extension of the notice file that will be created in the base directory.
   * <p>
   * This extension will be added to localPath.
   * <p>
   * For example, if you specify noticeExtension = '.err', and then you call:
   * <p>
   * <code>
   * tunel.writeNoticeLines("some/folder/MySupperConfig.hotconfig")
   * </code>
   * <p>
   * Then the notice file will be created by path
   * <p>
   * <code>
   * some/folder/MySupperConfig.hotconfig.err
   * </code>
   * <p>
   * Really file create by path
   *
   * @param noticeExtension The extension of the notice file
   * @return this
   */
  public @NonNull ConfigTunnelFileBuilder noticeExtension(@NonNull String noticeExtension) {
    this.noticeExtension = noticeExtension;
    return this;
  }

  /**
   * Builds a file-backed configuration tunnel.
   *
   * @return configured file tunnel
   * @throws IllegalArgumentException if {@link #baseDir(Path)} or {@link #noticeExtension(String)} was not specified
   */
  public @NonNull ConfigTunnelFile build() {
    if (baseDir == null) {
      throw new IllegalArgumentException("JYf0g5Hu6Y :: `baseDir` must be specified");
    }
    return new ConfigTunnelFile(new ConfigTunnelFile.Def(baseDir, noticeExtension));
  }
}
