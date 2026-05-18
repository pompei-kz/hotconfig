package kz.pompei.conf.core;

import java.time.Instant;
import kz.pompei.conf.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public interface ConfTunnel {

  @Nullable Conf read(@NonNull String localPath);

  void write(@NonNull String confPath, @NonNull Conf conf);

  @Nullable Instant lastModified(@NonNull String localPath);

}
