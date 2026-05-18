package kz.pompei.conf.core;

import java.nio.file.Path;
import java.time.Instant;
import kz.pompei.conf.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stored configuration in a file system.
 * <p>
 * The path to the configuration file is selected from basePath and localPath. At the very beginning of this file are comment lines for
 * the file itself, beginning with the # symbol. Then comes a blank line. Then comes the first parameter. Then another space.
 * Then comes the second parameter. And so on until the last parameter. The parameter begins with comment lines beginning with #.
 * Then comes a line with the parameter key, equals, and the parameter value—all on one line. If the parameter value contains a line break,
 * it is replaced with \n. The \ symbol is replaced with its repetition \\.
 */
public class ConfTunnelFile implements ConfTunnel {

  @NonNull private final Path baseDir;

  public ConfTunnelFile(@NonNull Path baseDir) {
    this.baseDir = baseDir;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    throw new RuntimeException("2026-05-18 13:07 Not impl yet ConfTunnelFile.read()");
  }

  @Override public void write(@NonNull String confPath, @NonNull Conf conf) {
    throw new RuntimeException("2026-05-18 13:07 Not impl yet ConfTunnelFile.write()");
  }

  @Override public @Nullable Instant lastModified(@NonNull String localPath) {
    throw new RuntimeException("2026-05-18 13:07 Not impl yet ConfTunnelFile.lastModified()");
  }
}
