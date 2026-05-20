package kz.pompei.hotconfig.core.model;

import lombok.NonNull;

public class HotConfFactoryParamsBuilder {
  private @NonNull String extension              = ".hotconf";
  private          long   revisionCheckTimeoutMs = 350;

  HotConfFactoryParamsBuilder() {}

  public HotConfFactoryParamsBuilder extension(@NonNull String extension) {
    this.extension = extension;
    return this;
  }

  public HotConfFactoryParamsBuilder revisionCheckTimeoutMs(long revisionCheckTimeoutMs) {
    this.revisionCheckTimeoutMs = revisionCheckTimeoutMs;
    return this;
  }

  public HotConfFactoryParams build() {
    return new HotConfFactoryParams(this.extension, this.revisionCheckTimeoutMs);
  }

  public String toString() {
    return getClass().getSimpleName() +
      "(extension=" + this.extension + ", revisionCheckTimeoutMs=" + this.revisionCheckTimeoutMs + ")";
  }
}
