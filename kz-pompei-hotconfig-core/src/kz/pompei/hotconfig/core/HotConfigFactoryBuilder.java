package kz.pompei.hotconfig.core;

import lombok.NonNull;

// TODO add Javadoc here and all public methods in this class
public class HotConfigFactoryBuilder {

  private          ConfigTunnel tunnel;
  private @NonNull Clock        clock                  = Clock.REAL;
  private @NonNull EnvSrc       envSrc                 = EnvSrc.REAL;
  private @NonNull String       extension              = ".hotconf";
  private          long         revisionCheckTimeoutMs = 2000;

  HotConfigFactoryBuilder() {}

  public @NonNull HotConfigFactoryBuilder clock(@NonNull Clock clock) {
    this.clock = clock;
    return this;
  }

  public @NonNull HotConfigFactoryBuilder envSrc(@NonNull EnvSrc envSrc) {
    this.envSrc = envSrc;
    return this;
  }

  public @NonNull HotConfigFactoryBuilder extension(@NonNull String extension) {
    this.extension = extension;
    return this;
  }

  public @NonNull HotConfigFactoryBuilder revisionCheckTimeoutMs(long revisionCheckTimeoutMs) {
    this.revisionCheckTimeoutMs = revisionCheckTimeoutMs;
    return this;
  }

  public @NonNull HotConfigFactoryBuilder tunnel(ConfigTunnel tunnel) {
    this.tunnel = tunnel;
    return this;
  }

  public HotConfigFactory build() {
    if (tunnel == null) {
      throw new IllegalArgumentException("r0E4Gx01Ek :: ConfigTunnel cannot be null");
    }
    return new HotConfigFactory(new HotConfigFactory.Def(tunnel, clock, envSrc, extension, revisionCheckTimeoutMs));
  }
}
