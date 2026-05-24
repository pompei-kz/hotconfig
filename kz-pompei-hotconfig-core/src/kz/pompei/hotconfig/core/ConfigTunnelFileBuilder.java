package kz.pompei.hotconfig.core;

import java.nio.file.Path;
import kz.pompei.hotconfig.core.ann.ConfFolder;
import lombok.NonNull;

// TODO add Javadoc here
public class ConfigTunnelFileBuilder {

  private Path   baseDir;
  private String noticeExtension;

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

  public @NonNull ConfigTunnelFile build() {
    if (baseDir == null) {
      throw new IllegalArgumentException("JYf0g5Hu6Y :: `baseDir` must be specified");
    }
    if (noticeExtension == null) {
      throw new IllegalArgumentException("WWO0PtL58H :: `noticeExtension` must be specified");
    }
    return new ConfigTunnelFile(new ConfigTunnelFile.Def(baseDir, noticeExtension));
  }
}
