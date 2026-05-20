package kz.pompei.conf.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import kz.pompei.conf.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTunnelFake implements ConfigTunnel {

  public record Dot(@NonNull Conf conf, long revision) {}

  public final  ConcurrentHashMap<String, Dot>           storage                 = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicInteger> readCount               = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicInteger> writeCount              = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicInteger> modificationMarkerCount = new ConcurrentHashMap<>();

  @Override public @Nullable Conf read(@NonNull String localPath) {

    readCount.computeIfAbsent(localPath, k -> new AtomicInteger(0)).incrementAndGet();

    Dot dot = storage.get(localPath);
    return dot == null ? null : dot.conf;
  }

  @Override public void write(@NonNull String localPath, @NonNull Conf conf) {

    writeCount.computeIfAbsent(localPath, k -> new AtomicInteger(0)).incrementAndGet();

    synchronized (storage) {
      Long revision    = Optional.of(storage).map(x->x.get(localPath)).map(x->x.revision).orElse(null);
      long newRevision = revision == null ? 1 : revision + 1;
      Dot  newDot      = new Dot(conf.copy(), newRevision);
      storage.put(localPath, newDot);
    }
  }

  @Override public @Nullable Long modificationMarker(@NonNull String localPath) {

    modificationMarkerCount.computeIfAbsent(localPath, k -> new AtomicInteger(0)).incrementAndGet();

    Dot dot = storage.get(localPath);
    return dot == null ? null : dot.revision;
  }

  public void clearCounts() {
    readCount.clear();
    writeCount.clear();
    modificationMarkerCount.clear();
  }

  public int readCount(@NonNull String localPath) {
    AtomicInteger x = readCount.get(localPath);
    return x == null ? 0 : x.intValue();
  }

  public int writeCount(@NonNull String localPath) {
    AtomicInteger x = writeCount.get(localPath);
    return x == null ? 0 : x.intValue();
  }

  public int modificationMarkerCount(@NonNull String localPath) {
    AtomicInteger x = modificationMarkerCount.get(localPath);
    return x == null ? 0 : x.intValue();
  }

  public void readCount_isEmpty() {
    assertThat(readCount).isEmpty();
  }

  public void writeCount_isEmpty() {
    assertThat(writeCount).isEmpty();
  }

  public void modificationMarkerCount_isEmpty() {
    assertThat(modificationMarkerCount).isEmpty();
  }
}
