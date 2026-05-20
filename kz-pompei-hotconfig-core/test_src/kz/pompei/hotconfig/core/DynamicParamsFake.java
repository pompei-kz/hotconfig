package kz.pompei.hotconfig.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class DynamicParamsFake implements DynamicParams {
  private final AtomicLong now = new AtomicLong(0);

  public DynamicParamsFake(long nowStarted) {
    now.set(nowStarted);
  }

  @Override public long now() {
    return now.longValue();
  }

  public final Map<String, String> envMap = new HashMap<>();

  @Override public @Nullable String env(@NonNull String envName) {
    return envMap.get(envName);
  }

  @SuppressWarnings("UnusedReturnValue")
  public DynamicParamsFake inc(long delta) {
    now.addAndGet(delta);
    return this;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(now=" + now + ")";
  }
}
