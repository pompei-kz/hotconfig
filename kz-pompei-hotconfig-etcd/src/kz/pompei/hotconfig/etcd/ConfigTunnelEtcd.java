package kz.pompei.hotconfig.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import kz.pompei.hotconfig.core.ConfigTunnel;
import kz.pompei.hotconfig.core.ParseUtil;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * Stores configuration data in etcd.
 * <p>
 * Connect it to a running etcd v3 server by building a jetcd client separately and passing it
 * to the builder. For the local Docker compose setup
 * in this repository, the endpoint is {@code http://localhost:17403}.
 * <p>
 * One configuration is stored under one etcd key. The value uses a line-oriented format:
 * the value stores the configuration file content in the same format as {@code ConfTunnelFile}.
 * The modification marker comes from the etcd key {@code modRevision}, so the stored value does
 * not need to carry a separate timestamp field.
 * <p>
 * Example stored values for two configurations:
 * <pre>{@code
 * /kz-pompei-conf-etcd/some/folder/app.conf
 * #application configuration
 *
 * #database
 * db.host=localhost
 *
 * db.port=5432
 * #cache
 * cache.enabled=true
 *
 * /kz-pompei-conf-etcd/some/folder/other.conf
 * #other configuration
 *
 * api.url=https://example.org
 *
 * api.timeout=5000
 * }</pre>
 * <p>
 * Example:
 * <pre>{@code
 * try (ConfigTunnelEtcd tunnel = ConfigTunnelEtcd.builder().endpoints("http://localhost:17403").build()) {
 *   Conf conf = tunnel.read("some/folder/test-config.hotconf");
 * }
 * }</pre>
 */
public class ConfigTunnelEtcd implements ConfigTunnel, AutoCloseable {
  private static final String ERROR_PREFIX = "#ERROR ";

  @NonNull private final Def def;
  @NonNull private final KV  kvClient;

  public static @NonNull ConfigTunnelEtcdBuilder builder() {
    return new ConfigTunnelEtcdBuilder();
  }

  /**
   * Creates an etcd tunnel using the provided builder definition.
   *
   * @param def etcd connection and key layout settings.
   */
  ConfigTunnelEtcd(@NonNull Def def) {
    this.def      = def;
    this.kvClient = def.client.getKVClient();
  }

  @RequiredArgsConstructor
  static class Def {
    final @NonNull Client client;
    final @NonNull String keyPrefix;
    final @NonNull String noticeExtension;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    String stored = get(key(localPath));
    if (stored == null) return null;

    return parse(stored);
  }

  @Override public void write(@NonNull String localPath, @NonNull Conf conf) {
    put(key(localPath), serialize(conf));
  }

  @Override public @NonNull List<String> readNoticeLines(@NonNull String localPath) {
    String stored = get(noticeKey(localPath));
    if (stored == null || stored.isEmpty()) return List.of();
    return List.of(stored.split("\n", -1));
  }

  @Override public void writeNoticeLines(@NonNull String localPath, @NonNull List<String> lines) {
    String key = noticeKey(localPath);
    if (lines.isEmpty()) {
      delete(key);
      return;
    }
    put(key, String.join("\n", lines));
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {
    return modificationMarkerByKey(key(localPath));
  }

  @Override public void close() {
    def.client.close();
  }

  private @NonNull String key(@NonNull String localPath) {
    String normalized = localPath.startsWith("/") ? localPath.substring(1) : localPath;
    return def.keyPrefix + normalized;
  }

  private @NonNull String noticeKey(@NonNull String localPath) {
    return key(localPath + def.noticeExtension);
  }

  private @Nullable String get(@NonNull String key) {
    try {
      List<KeyValue> kvs = kvClient.get(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8))).get().getKvs();
      if (kvs.isEmpty()) return null;
      return new String(kvs.get(0).getValue().getBytes(), StandardCharsets.UTF_8);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("R1s2T3u4V5 :: Could not read etcd key: " + key, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("W6x7Y8z9A0 :: Could not read etcd key: " + key, e);
    }
  }

  private @Nullable Long modificationMarkerByKey(@NonNull String key) {
    try {
      List<KeyValue> kvs = kvClient.get(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8))).get().getKvs();
      if (kvs.isEmpty()) return null;
      return kvs.get(0).getModRevision();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("B1c2D3e4F5 :: Could not read etcd revision: " + key, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("G6h7I8j9K0 :: Could not read etcd revision: " + key, e);
    }
  }

  private void put(@NonNull String key, @NonNull String value) {
    try {
      kvClient.put(
        ByteSequence.from(key.getBytes(StandardCharsets.UTF_8)),
        ByteSequence.from(value.getBytes(StandardCharsets.UTF_8))
      ).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("L1m2N3o4P5 :: Could not write etcd key: " + key, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Q6r7S8t9U0 :: Could not write etcd key: " + key, e);
    }
  }

  private void delete(@NonNull String key) {
    try {
      kvClient.delete(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8))).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("V1w2X3y4Z5 :: Could not delete etcd key: " + key, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("A6b7C8d9E0 :: Could not delete etcd key: " + key, e);
    }
  }

  private @NonNull String serialize(@NonNull Conf conf) {
    return serializeConf(conf);
  }

  private @NonNull Conf parse(@NonNull String stored) {
    return parseConf(stored);
  }

  private @NonNull String serializeConf(@NonNull Conf conf) {
    List<String> lines = new ArrayList<>();
    for (String comment : conf.confComments) lines.add(writeComment(comment));
    lines.add("");

    for (int i = 0; i < conf.params.size(); i++) {
      ConfParam param = conf.params.get(i);
      for (String comment : param.comments) lines.add(writeComment(comment));
      lines.add(param.name + "=" + escape(param.valueStr));
      if (param.error != null) {
        for (String errorLine : param.error.split("\n", -1)) lines.add(ERROR_PREFIX + errorLine);
      }
      if (i < conf.params.size() - 1) lines.add("");
    }
    return String.join("\n", lines);
  }

  private @NonNull Conf parseConf(@NonNull String body) {
    List<String> lines = body.isEmpty() ? List.of() : List.of(body.split("\n", -1));
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

      if (index >= lines.size()) {
        break;
      }
      if (lines.get(index).isBlank()) {
        index = skipBlankLines(lines, index);
        continue;
      }

      String line  = lines.get(index);
      int    split = line.indexOf('=');
      if (split < 0) {
        throw new IllegalArgumentException("Invalid etcd configuration payload: missing '=' in parameter line");
      }
      param.name     = line.substring(0, split);
      param.valueStr = ParseUtil.unescape(line.substring(split + 1));
      index++;

      StringBuilder error    = new StringBuilder();
      boolean       hasError = false;
      while (index < lines.size() && lines.get(index).startsWith(ERROR_PREFIX)) {
        hasError = true;
        if (!error.isEmpty()) error.append('\n');
        error.append(lines.get(index).substring(ERROR_PREFIX.length()));
        index++;
      }
      if (hasError) param.error = error.toString();
      conf.params.add(param);

      index = skipBlankLines(lines, index);
    }

    return conf;
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

  private @NonNull String escape(@Nullable String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("\n", "\\n");
  }

}
