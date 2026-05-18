package kz.pompei.conf.etcd.tst_utils;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import kz.pompei.conf.etcd.ConfTunnelEtcdDef;
import lombok.NonNull;

public abstract class EtcdTestParent {

  protected static final String ENDPOINT = "http://localhost:17403";
  protected static final String KEY_PREFIX = "/kz-pompei-conf-etcd/";

  protected @NonNull ConfTunnelEtcdDef createParams(@NonNull String testName) {
    ConfTunnelEtcdDef params = new ConfTunnelEtcdDef();
    params.keyPrefix = KEY_PREFIX + testName + "_" + Long.toUnsignedString(System.nanoTime()) + "/";
    return params;
  }

  protected @NonNull Client createClient() {
    return Client.builder().endpoints(ENDPOINT).build();
  }

  protected boolean keyExists(@NonNull Client client, @NonNull String key) {
    try {
      GetResponse response = client.getKVClient().get(byteSequence(key)).get();
      return !response.getKvs().isEmpty();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("vB3xQ7dN2m :: Could not check etcd key existence: " + key, e);
    }
    catch (ExecutionException e) {
      throw new RuntimeException("vB3xQ7dN2m :: Could not check etcd key existence: " + key, e);
    }
  }

  protected void deleteKey(@NonNull Client client, @NonNull String key) {
    try {
      client.getKVClient().delete(byteSequence(key)).get();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("fY8uP2wL9a :: Could not delete etcd key: " + key, e);
    }
    catch (ExecutionException e) {
      throw new RuntimeException("fY8uP2wL9a :: Could not delete etcd key: " + key, e);
    }
  }

  protected @NonNull String key(@NonNull ConfTunnelEtcdDef params, @NonNull String localPath) {
    String normalized = localPath.startsWith("/") ? localPath.substring(1) : localPath;
    return params.keyPrefix + normalized;
  }

  protected ByteSequence byteSequence(@NonNull String value) {
    return ByteSequence.from(value.getBytes(StandardCharsets.UTF_8));
  }
}
