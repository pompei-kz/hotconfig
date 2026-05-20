package kz.pompei.hotconfig.etcd.tst_utils;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import kz.pompei.hotconfig.etcd.ConfTunnelEtcdDef;
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
      throw new RuntimeException("X1y2Z3a4B5 :: Could not check etcd key existence: " + key, e);
    }
    catch (ExecutionException e) {
      throw new RuntimeException("C6d7E8f9G0 :: Could not check etcd key existence: " + key, e);
    }
  }

  protected void deleteKey(@NonNull Client client, @NonNull String key) {
    try {
      client.getKVClient().delete(byteSequence(key)).get();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("H1i2J3k4L5 :: Could not delete etcd key: " + key, e);
    }
    catch (ExecutionException e) {
      throw new RuntimeException("M6n7O8p9Q0 :: Could not delete etcd key: " + key, e);
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
