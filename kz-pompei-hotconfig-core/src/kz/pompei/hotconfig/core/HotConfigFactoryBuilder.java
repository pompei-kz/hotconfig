package kz.pompei.hotconfig.core;

import lombok.NonNull;

/**
 * Builder for {@link HotConfigFactory}.
 * <p>
 * A tunnel is required. Clock, environment source, file extension, and revision check timeout have production defaults and can be
 * overridden for tests or custom runtime behavior.
 */
public class HotConfigFactoryBuilder {

  private          ConfigTunnel tunnel;
  private @NonNull Clock        clock                  = Clock.REAL;
  private @NonNull EnvSrc       envSrc                 = EnvSrc.REAL;
  private @NonNull String       extension              = ".hotconf";
  private          long         revisionCheckTimeoutMs = 2000;

  HotConfigFactoryBuilder() {}

  /**
   * Sets the clock used for refresh timeout checks.
   *
   * @param clock clock implementation
   * @return this builder
   */
  public @NonNull HotConfigFactoryBuilder clock(@NonNull Clock clock) {
    this.clock = clock;
    return this;
  }

  /**
   * Sets the environment source used to resolve {@code $ENV{name}} placeholders in parameter values.
   *
   * @param envSrc environment source
   * @return this builder
   */
  public @NonNull HotConfigFactoryBuilder envSrc(@NonNull EnvSrc envSrc) {
    this.envSrc = envSrc;
    return this;
  }

  /**
   * Sets the extension appended to configuration interface simple names when forming storage paths.
   *
   * @param extension config file extension, for example {@code .hotconf}
   * @return this builder
   */
  public @NonNull HotConfigFactoryBuilder extension(@NonNull String extension) {
    this.extension = extension;
    return this;
  }

  /**
   * Sets how long cached configuration values can be reused before checking the tunnel modification marker.
   *
   * @param revisionCheckTimeoutMs timeout in milliseconds
   * @return this builder
   */
  public @NonNull HotConfigFactoryBuilder revisionCheckTimeoutMs(long revisionCheckTimeoutMs) {
    this.revisionCheckTimeoutMs = revisionCheckTimeoutMs;
    return this;
  }

  /**
   * Sets the storage tunnel used to read and write configuration data.
   *
   * @param tunnel config storage tunnel
   * @return this builder
   */
  public @NonNull HotConfigFactoryBuilder tunnel(ConfigTunnel tunnel) {
    this.tunnel = tunnel;
    return this;
  }

  /**
   * Builds the factory.
   *
   * @return configured hot config factory
   * @throws IllegalArgumentException if tunnel was not specified
   */
  public HotConfigFactory build() {
    if (tunnel == null) {
      throw new IllegalArgumentException("r0E4Gx01Ek :: ConfigTunnel cannot be null");
    }
    return new HotConfigFactory(new HotConfigFactory.Def(tunnel, clock, envSrc, extension, revisionCheckTimeoutMs));
  }
}
