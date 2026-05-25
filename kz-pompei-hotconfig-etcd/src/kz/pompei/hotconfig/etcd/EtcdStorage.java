package kz.pompei.hotconfig.etcd;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

interface EtcdStorage extends AutoCloseable {

  @Nullable String get(@NonNull String key);

  @Nullable Long modificationMarker(@NonNull String key);

  void put(@NonNull String key, @NonNull String value);

  void delete(@NonNull String key);

  @Override void close();

}
