package kz.pompei.conf.etcd;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

interface EtcdStorage extends AutoCloseable {

  @Nullable String get(@NonNull String key);

  void put(@NonNull String key, @NonNull String value);

  @Override void close();

}
