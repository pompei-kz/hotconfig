package kz.pompei.conf.jdbc;

import java.time.Instant;
import kz.pompei.conf.core.ConfTunnel;
import kz.pompei.conf.core.model.Conf;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Данный класс позволяет хранить конфигурации в базе данных.
 * <p>
 * Конфигурации хранятся в отдельной таблице с именем params.tableName.
 * <p>
 * Если этой таблицы в БД нет, то она должна создастся автоматически при первом обращении к ней.
 *
 */
public abstract class ConfTunnelJdbc implements ConfTunnel {

  @NonNull protected final ConfTunnelJdbcDef params;
  @NonNull protected final ConnectionGet     connectionGet;

  protected ConfTunnelJdbc(@NonNull ConnectionGet connectionGet, @NonNull ConfTunnelJdbcDef params) {
    this.params        = params;
    this.connectionGet = connectionGet;
  }

  @Override public @Nullable Conf read(@NonNull String localPath) {
    throw new RuntimeException("2026-05-18 15:34 Not impl yet ConfTunnelJdbc.read()");
  }

  @Override public void write(@NonNull String confPath, @NonNull Conf conf) {
    throw new RuntimeException("2026-05-18 15:34 Not impl yet ConfTunnelJdbc.write()");
  }

  @Override public @Nullable Instant lastModified(@NonNull String localPath) {
    throw new RuntimeException("2026-05-18 15:34 Not impl yet ConfTunnelJdbc.lastModified()");
  }
}
