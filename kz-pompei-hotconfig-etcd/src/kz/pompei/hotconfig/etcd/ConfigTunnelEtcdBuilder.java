package kz.pompei.hotconfig.etcd;

import io.etcd.jetcd.Client;
import lombok.NonNull;

public class ConfigTunnelEtcdBuilder {

  private          Client client;
  private @NonNull String keyPrefix       = "/kz-pompei-conf-etcd/";
  private @NonNull String noticeExtension = ".notice";

  /**
   * Extension appended to the configuration key when storing notice lines.
   */
  public @NonNull ConfigTunnelEtcdBuilder noticeExtension(@NonNull String noticeExtension) {
    this.noticeExtension = noticeExtension;
    return this;
  }

  public @NonNull ConfigTunnelEtcdBuilder client(@NonNull Client client) {
    this.client = client;
    return this;
  }

  public @NonNull ConfigTunnelEtcdBuilder endpoints(@NonNull String endpoints) {
    client = Client.builder().endpoints(endpoints).build();
    return this;
  }

  /**
   * Prefix under which configuration keys are stored in etcd.
   * <p>
   * The final key is composed as {@code keyPrefix + localPath}.
   */
  public @NonNull ConfigTunnelEtcdBuilder keyPrefix(@NonNull String keyPrefix) {
    this.keyPrefix = keyPrefix;
    return this;
  }

  public @NonNull String keyPrefix() {
    return keyPrefix;
  }

  public @NonNull String noticeExtension() {
    return noticeExtension;
  }

  public ConfigTunnelEtcd build() {
    if (client == null) {
      throw new IllegalArgumentException("Hf3Lp9Qw2E :: `client` must be specified");
    }
    return new ConfigTunnelEtcd(new ConfigTunnelEtcd.Def(client, keyPrefix, noticeExtension));
  }
}
