package kz.pompei.hotconfig.core;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kz.pompei.hotconfig.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class ConfigTunnelMem implements ConfigTunnel {

  private final ConcurrentHashMap<String, Conf>         store       = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<String>> noticeLines = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long>         modMarkers  = new ConcurrentHashMap<>();
  private final AtomicLong                              nextMarker  = new AtomicLong(1L);

  @Override public @Nullable Conf read(@NonNull String localPath) {
    Conf conf = store.get(localPath);
    return conf == null ? null : conf.copy();
  }

  @Override public @NonNull List<String> readNoticeLines(@NonNull String localPath) {
    List<String> list = noticeLines.get(localPath);
    return list == null ? List.of() : list;
  }

  @Override public void write(@NonNull String localPath, @NonNull Conf conf) {
    store.put(localPath, conf.copy());
    modMarkers.put(localPath, nextMarker.getAndIncrement());
  }

  @Override public void writeNoticeLines(@NonNull String localPath, @NonNull List<String> lines) {
    noticeLines.put(localPath, List.copyOf(lines));
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {
    return modMarkers.get(localPath);
  }
}
