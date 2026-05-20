package kz.pompei.conf.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
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
public class ConfigTunnelFile implements ConfigTunnel {

  @NonNull private final Path baseDir;

  public ConfigTunnelFile(@NonNull Path baseDir) {
    this.baseDir = baseDir;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    Path path = path(localPath);
    if (!Files.exists(path)) return null;

    try {
      List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      Conf         conf  = new Conf();
      int          index = 0;

      while (index < lines.size() && isComment(lines.get(index))) {
        conf.confComments.add(readComment(lines.get(index)));
        index++;
      }

      index = skipBlankLines(lines, index);

      while (index < lines.size()) {
        ConfParam param = new ConfParam();
        while (index < lines.size() && isComment(lines.get(index))) {
          param.comments.add(readComment(lines.get(index)));
          index++;
        }

        if (index >= lines.size()) break;
        if (lines.get(index).isBlank()) {
          index = skipBlankLines(lines, index);
          continue;
        }

        String line  = lines.get(index);
        int    split = line.indexOf('=');
        if (split < 0) {
          throw new IllegalArgumentException("A1b2C3d4E5 :: Invalid configuration parameter line: " + line);
        }
        param.name     = line.substring(0, split);
        param.valueStr = unescape(line.substring(split + 1));
        conf.params.add(param);

        index = skipBlankLines(lines, index + 1);
      }

      return conf;
    } catch (IOException e) {
      throw new RuntimeException("F6g7H8i9J0 :: Could not read configuration file: " + path, e);
    }
  }

  @Override public void write(@NonNull String localPath, @NonNull Conf conf) {
    Path path = path(localPath);
    try {
      Path parent = path.getParent();
      if (parent != null) Files.createDirectories(parent);
      Files.write(path, writeLines(conf), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("K1l2M3n4O5 :: Could not write configuration file: " + path, e);
    }
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {
    Path path = path(localPath);
    if (!Files.exists(path)) return null;

    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException e) {
      throw new RuntimeException("P6q7R8s9T0 :: Could not get configuration file modification time: " + path, e);
    }
  }

  private Path path(@NonNull String localPath) {
    return baseDir.resolve(localPath);
  }

  private List<String> writeLines(@NonNull Conf conf) {
    List<String> lines = new ArrayList<>();
    for (String comment : conf.confComments) lines.add(writeComment(comment));
    lines.add("");

    for (int i = 0; i < conf.params.size(); i++) {
      ConfParam param = conf.params.get(i);
      for (String comment : param.comments) lines.add(writeComment(comment));
      lines.add(param.name + "=" + escape(param.valueStr));
      if (i < conf.params.size() - 1) lines.add("");
    }
    return lines;
  }

  private boolean isComment(@NonNull String line) {
    return line.startsWith("#");
  }

  private String readComment(@NonNull String line) {
    return line.substring(1);
  }

  private String writeComment(@Nullable String comment) {
    return "#" + (comment == null ? "" : comment);
  }

  private int skipBlankLines(@NonNull List<String> lines, int index) {
    while (index < lines.size() && lines.get(index).isBlank()) index++;
    return index;
  }

  private String escape(@Nullable String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("\n", "\\n");
  }

  private String unescape(@NonNull String value) {
    StringBuilder result = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '\\' && i + 1 < value.length()) {
        char next = value.charAt(++i);
        if (next == 'n') {
          result.append('\n');
        } else {
          result.append(next);
        }
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
}
