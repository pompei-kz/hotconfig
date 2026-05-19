package kz.pompei.conf.core.model;

import lombok.NonNull;

public class HotConfFactoryParams {
  public final @NonNull String extension;
  public final          long   revisionCheckTimeoutMs;

  HotConfFactoryParams(@NonNull String extension, long revisionCheckTimeoutMs) {
    this.extension              = extension;
    this.revisionCheckTimeoutMs = revisionCheckTimeoutMs;
  }

  public static @NonNull HotConfFactoryParamsBuilder builder() {
    return new HotConfFactoryParamsBuilder();
  }
}
