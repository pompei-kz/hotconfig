package kz.pompei.conf.core;

import kz.pompei.conf.core.model.HotConfFactoryParams;
import lombok.NonNull;

public class HotConfFactory {
  @NonNull private final ConfTunnel           confTunnel;
  @NonNull private final HotConfFactoryParams params;
  private final          Clock                clock;

  public HotConfFactory(@NonNull ConfTunnel confTunnel, @NonNull HotConfFactoryParams params, @NonNull Clock clock) {
    this.confTunnel = confTunnel;
    this.params     = params;
    this.clock      = clock;
  }

  public HotConfFactory(@NonNull ConfTunnel confTunnel) {
    this(confTunnel, HotConfFactoryParams.builder().build(), Clock.REAL);
  }

  public @NonNull <I> I createConf(@NonNull Class<I> confClass) {
    // TODO realize this method
    throw new RuntimeException("uaRIc6zZk1 :: Method not implemented yet");
  }
}
