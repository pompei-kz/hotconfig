package kz.pompei.conf.core;

import java.util.concurrent.atomic.AtomicLong;

public class ClockFake implements Clock {
  private final AtomicLong now = new AtomicLong(0);

  @Override public long now() {
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
