package kz.pompei.hotconfig.core.model;

import lombok.NonNull;

public class HotConfigFactoryParamsBuilder {
  private @NonNull String extension              = ".hotconf";
  private          long   revisionCheckTimeoutMs = 350;

  HotConfigFactoryParamsBuilder() {}

  public HotConfigFactoryParamsBuilder extension(@NonNull String extension) {
    this.extension = extension;
    return this;
  }

  public HotConfigFactoryParamsBuilder revisionCheckTimeoutMs(long revisionCheckTimeoutMs) {
    this.revisionCheckTimeoutMs = revisionCheckTimeoutMs;
    return this;
  }

  public HotConfigFactoryParams build() {
    return new HotConfigFactoryParams(this.extension, this.revisionCheckTimeoutMs);
  }

  public String toString() {
    return getClass().getSimpleName() +
      "(extension=" + this.extension + ", revisionCheckTimeoutMs=" + this.revisionCheckTimeoutMs + ")";
  }
}
