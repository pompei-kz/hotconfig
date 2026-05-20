package kz.pompei.hotconfig.core.model;

import lombok.NonNull;

public class HotConfigFactoryParams {
  public final @NonNull String extension;
  public final          long   revisionCheckTimeoutMs;

  HotConfigFactoryParams(@NonNull String extension, long revisionCheckTimeoutMs) {
    this.extension              = extension;
    this.revisionCheckTimeoutMs = revisionCheckTimeoutMs;
  }

  public static @NonNull HotConfigFactoryParamsBuilder builder() {
    return new HotConfigFactoryParamsBuilder();
  }
}
