package app;

import java.util.HashMap;
import java.util.Map;
import kz.pompei.hotconfig.core.EnvSrc;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class EnvSrcFake implements EnvSrc {

  public final Map<String, String> envMap = new HashMap<>();

  @Override public @Nullable String env(@NonNull String envName) {
    return envMap.get(envName);
  }
}
