package kz.pompei.hotconfig.core;

import java.util.concurrent.atomic.AtomicLong;

public class ClockFake implements Clock {
  private final AtomicLong now = new AtomicLong(0);

  public ClockFake(long nowStarted) {
    now.set(nowStarted);
  }

  @Override public long nowMs() {
    return now.longValue();
  }

  @SuppressWarnings("UnusedReturnValue")
  public ClockFake inc(long delta) {
    now.addAndGet(delta);
    return this;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(now=" + now + ")";
  }
}
