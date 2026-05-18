package kz.pompei.conf.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import kz.pompei.conf.core.ConfTunnel;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores configuration data in etcd.
 * <p>
 * Connect it to a running etcd v3 server by building a jetcd client separately and passing it
 * to the constructor together with the storage parameters. For the local Docker compose setup
 * in this repository, the endpoint is {@code http://localhost:17403}.
 * <p>
 * One configuration is stored under one etcd key. The value uses a line-oriented format:
 * the first line stores the last modification timestamp in epoch milliseconds, and the
 * remaining lines store the configuration file content in the same format as {@code ConfTunnelFile}.
 * <p>
 * Example stored values for two configurations:
 * <pre>{@code
 * /kz-pompei-conf-etcd/some/folder/app.conf
 * lastModifiedMillis=1716038400000
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
 * lastModifiedMillis=1716038500000
 * #other configuration
 *
 * api.url=https://example.org
 *
 * api.timeout=5000
 * }</pre>
 * <p>
 * Example:
 * <pre>{@code
 * ConfTunnelEtcdDef params = new ConfTunnelEtcdDef();
 * try (Client client = Client.builder().endpoints("http://localhost:17403").build();
 *      ConfTunnelEtcd tunnel = new ConfTunnelEtcd(client, params)) {
 *   Conf conf = tunnel.read("some/folder/test-config.hotconf");
 * }
 * }</pre>
 */
public class ConfTunnelEtcd implements ConfTunnel, AutoCloseable {

  private static final String LAST_MODIFIED_PREFIX = "lastModifiedMillis=";

  @NonNull private final ConfTunnelEtcdDef params;
  @NonNull private final EtcdStorage storage;

  /**
   * Creates an etcd tunnel using the provided jetcd client and storage parameters.
   *
   * @param client jetcd client connected by the caller
   * @param params etcd connection and key layout settings
   */
  public ConfTunnelEtcd(@NonNull Client client, @NonNull ConfTunnelEtcdDef params) {
    this(params, new JetcdStorage(client));
  }

  ConfTunnelEtcd(@NonNull ConfTunnelEtcdDef params, @NonNull EtcdStorage storage) {
    this.params = params;
    this.storage = storage;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    String stored = storage.get(key(localPath));
    if (stored == null) return null;

    ParsedValue parsedValue = parse(stored);
    return parsedValue.conf;
  }

  @Override public void write(@NonNull String confPath, @NonNull Conf conf) {
    storage.put(key(confPath), serialize(System.currentTimeMillis(), conf));
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {
    String stored = storage.get(key(localPath));
    if (stored == null) return null;
    return parse(stored).lastModified;
  }

  @Override public void close() {
    storage.close();
  }

  private String key(@NonNull String localPath) {
    String normalized = localPath.startsWith("/") ? localPath.substring(1) : localPath;
    return params.keyPrefix + normalized;
  }

  private String serialize(long lastModifiedMillis, @NonNull Conf conf) {
    StringBuilder builder = new StringBuilder();
    builder.append(LAST_MODIFIED_PREFIX).append(lastModifiedMillis).append('\n');
    builder.append(serializeConf(conf));
    return builder.toString();
  }

  private ParsedValue parse(@NonNull String stored) {
    int newline = stored.indexOf('\n');
    String firstLine = newline < 0 ? stored : stored.substring(0, newline);
    String body = newline < 0 ? "" : stored.substring(newline + 1);

    if (!firstLine.startsWith(LAST_MODIFIED_PREFIX)) {
      throw new IllegalArgumentException("Invalid etcd configuration payload: missing last modified prefix");
    }

    long lastModifiedMillis = Long.parseLong(firstLine.substring(LAST_MODIFIED_PREFIX.length()));
    return new ParsedValue(lastModifiedMillis, parseConf(body));
  }

  private String serializeConf(@NonNull Conf conf) {
    List<String> lines = new ArrayList<>();
    for (String comment : conf.confComments) lines.add(writeComment(comment));
    lines.add("");

    for (int i = 0; i < conf.params.size(); i++) {
      ConfParam param = conf.params.get(i);
      for (String comment : param.comments) lines.add(writeComment(comment));
      lines.add(param.name + "=" + escape(param.valueStr));
      if (i < conf.params.size() - 1) lines.add("");
    }
    return String.join("\n", lines);
  }

  private Conf parseConf(@NonNull String body) {
    List<String> lines = body.isEmpty() ? List.of() : List.of(body.split("\n", -1));
    Conf conf = new Conf();
    int index = 0;

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

      String line = lines.get(index);
      int split = line.indexOf('=');
      if (split < 0) {
        throw new IllegalArgumentException("Invalid etcd configuration payload: missing '=' in parameter line");
      }
      param.name = line.substring(0, split);
      param.valueStr = unescape(line.substring(split + 1));
      conf.params.add(param);

      index = skipBlankLines(lines, index + 1);
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
        if (next == 'n') result.append('\n');
        else result.append(next);
      }
      else result.append(c);
    }
    return result.toString();
  }

  private static final class ParsedValue {
    private final Long lastModified;
    private final Conf conf;

    private ParsedValue(@NonNull Long lastModified, @NonNull Conf conf) {
      this.lastModified = Objects.requireNonNull(lastModified);
      this.conf = Objects.requireNonNull(conf);
    }
  }

  private static final class JetcdStorage implements EtcdStorage {

    @NonNull private final Client client;
    @NonNull private final KV kvClient;

    private JetcdStorage(@NonNull Client client) {
      this.client = client;
      this.kvClient = client.getKVClient();
    }

    @Override public @Nullable String get(@NonNull String key) {
      try {
        List<KeyValue> kvs = kvClient.get(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8))).get().getKvs();
        if (kvs.isEmpty()) return null;
        return new String(kvs.get(0).getValue().getBytes(), StandardCharsets.UTF_8);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("R9z5qN2wAa :: Could not read etcd key: " + key, e);
      }
      catch (ExecutionException e) {
        throw new RuntimeException("R9z5qN2wAa :: Could not read etcd key: " + key, e);
      }
    }

    @Override public void put(@NonNull String key, @NonNull String value) {
      try {
        kvClient.put(
          ByteSequence.from(key.getBytes(StandardCharsets.UTF_8)),
          ByteSequence.from(value.getBytes(StandardCharsets.UTF_8))
        ).get();
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("T7k4Vd8pLm :: Could not write etcd key: " + key, e);
      }
      catch (ExecutionException e) {
        throw new RuntimeException("T7k4Vd8pLm :: Could not write etcd key: " + key, e);
      }
    }

    @Override public void close() {
      client.close();
    }
  }
}
