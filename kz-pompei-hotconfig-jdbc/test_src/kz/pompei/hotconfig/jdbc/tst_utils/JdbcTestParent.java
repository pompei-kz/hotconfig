package kz.pompei.hotconfig.jdbc.tst_utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import kz.pompei.hotconfig.jdbc.ConnectionGet;
import kz.pompei.hotconfig.jdbc.DatabaseType;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.DataProvider;

import static java.util.Objects.requireNonNull;

public abstract class JdbcTestParent {

  @DataProvider(name = "databaseType")
  public Object[][] databaseType() {
    return Arrays.stream(DatabaseType.values()).map(t -> new Object[]{t}).toArray(Object[][]::new);
  }

  private static final Map<DatabaseType, DbConnectTemplate> DB_PARAMS;

  static {
    Map<DatabaseType, DbConnectTemplate> map = new HashMap<>();

    map.put(DatabaseType.PostgreSQL, new DbConnectTemplate(
      "jdbc:postgresql://localhost:17401/pompei_conf_db?applicationName=kz-pompei-conf{ROCK}",
      "pompei_conf",
      "CFcFYhe92yj0bkCynj5K"
    ));

    map.put(DatabaseType.MariaDB, new DbConnectTemplate(
      "jdbc:mariadb://localhost:17402/kz-pompei-conf?useUnicode=true&characterEncoding=utf8mb4&programName=kz-pompei-conf{ROCK}",
      "kz-pompei-conf",
      "19LpOmDjST6rs9DeADTc"
    ));

    DB_PARAMS = Map.copyOf(map);
  }

  private static @NonNull DbConnectBuilder dcBuilder(@NonNull DatabaseType databaseType, @Nullable String rock) {
    DbConnectTemplate dbConnectTemplate = requireNonNull(DB_PARAMS.get(databaseType));
    String            urlTemplate       = dbConnectTemplate.urlTemplate;
    String            replaceWith       = rock == null ? "" : "-" + rock;
    String            url               = urlTemplate.replaceAll("\\{ROCK}", replaceWith);
    String            username          = dbConnectTemplate.username;
    String            password          = dbConnectTemplate.password;
    return new DbConnectBuilder(url, username, password);
  }

  protected @NonNull ConnectionGet createConnectionGet(@NonNull DatabaseType databaseType, String rock) {
    DbConnectBuilder dcBuilder = dcBuilder(databaseType, rock);
    return dcBuilder::build;
  }

}
