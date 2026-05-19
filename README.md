# kz-pompei-conf

`kz-pompei-conf` is a Java 21 hot-configuration library. It creates a typed proxy from a Java interface, stores the backing configuration through a pluggable tunnel, and refreshes values when the storage revision changes.

The project currently provides storage tunnels for:

- local files
- JDBC databases: PostgreSQL and MariaDB
- etcd v3 through jetcd

Current project version: `0.0.1`.

## Quick Start

Create a configuration interface. This example follows the same pattern used in `HotConfFactoryTest`: the interface has a folder, config-level documentation, parameter documentation, and default values.

```java
import kz.pompei.conf.core.ann.ConfDefaultValue;
import kz.pompei.conf.core.ann.ConfDoc;
import kz.pompei.conf.core.ann.ConfFolder;

@ConfDoc("about1\nabout2\nabout3")
@ConfFolder("cool/folder")
public interface TestConf1 {

  @ConfDoc("description1\ndescription2")
  @ConfDefaultValue("def value 1")
  String param1();

  @ConfDoc("description3\ndescription4\ndescription5")
  @ConfDefaultValue("def value 2")
  String param2();
}
```

Create a `HotConfFactory` with `ConfTunnelFile`:

```java
import java.nio.file.Path;
import kz.pompei.conf.core.ConfTunnelFile;
import kz.pompei.conf.core.DynamicParams;
import kz.pompei.conf.core.HotConfFactory;
import kz.pompei.conf.core.model.HotConfFactoryParams;

Path baseDir = Path.of("./conf");

HotConfFactoryParams params = HotConfFactoryParams.builder()
  .extension(".tst")
  .revisionCheckTimeoutMs(500)
  .build();

HotConfFactory factory = new HotConfFactory(
  new ConfTunnelFile(baseDir),
  params,
  DynamicParams.REAL
);

TestConf1 conf = factory.createConf(TestConf1.class);

String param1 = conf.param1(); // "def value 1"
String param2 = conf.param2(); // "def value 2"
```

On the first read, if the file does not exist, the library creates:

```text
./conf/cool/folder/TestConf1.tst
```

With this content:

```text
#about1
#about2
#about3

#description1
#description2
param1=def value 1

#description3
#description4
#description5
param2=def value 2
```

Edit the file manually:

```text
param1=SKY TREE
param2=Flight near the star
```

And read new values

```java

String updatedParam1 = conf.param1(); // "SKY TREE"
String updatedParam2 = conf.param2(); // "Flight near the star"
```

## Modules

| Module                | Purpose                                                                       |
|-----------------------|-------------------------------------------------------------------------------|
| `kz-pompei-conf-core` | Core API, proxy factory, file tunnel, parser, annotations, and model classes. |
| `kz-pompei-conf-jdbc` | JDBC tunnel for PostgreSQL and MariaDB.                                       |
| `kz-pompei-conf-etcd` | etcd tunnel backed by jetcd.                                                  |
| `utils`               | Test utilities used by the repository tests.                                  |

## Requirements

- JDK 21
- Gradle wrapper from this repository
- Docker, only if you want to run PostgreSQL, MariaDB, or etcd integration tests

## Build And Test

Run the core tests:

```bash
./gradlew :kz-pompei-conf-core:test
```

Run all tests:

```bash
./gradlew test
```

Integration tests for JDBC and etcd expect local services. The repository includes a Docker Compose setup in `docker/docker-compose.yaml`; set `APP_DATA_ROOT` before starting it.

```bash
export APP_DATA_ROOT=/tmp/kz-pompei-conf-data
docker compose -f docker/docker-compose.yaml up -d
./gradlew test
```

## Basic Usage

Define a configuration interface:

```java
import kz.pompei.conf.core.ann.ConfDefaultValue;
import kz.pompei.conf.core.ann.ConfDoc;
import kz.pompei.conf.core.ann.ConfFolder;

@ConfFolder("app")
@ConfDoc("Application configuration")
public interface AppConf {
  @ConfDoc("HTTP port")
  @ConfDefaultValue("8080")
  int port();

  @ConfDefaultValue("true")
  boolean enabled();

  @ConfDefaultValue("localhost")
  String host();
}
```

Create a proxy backed by files:

```java
import java.nio.file.Path;
import kz.pompei.conf.core.ConfTunnelFile;
import kz.pompei.conf.core.HotConfFactory;

HotConfFactory factory = new HotConfFactory(new ConfTunnelFile(Path.of("./conf")));
AppConf conf = factory.createConf(AppConf.class);

int port = conf.port();
boolean enabled = conf.enabled();
String host = conf.host();
```

The file path is built from `@ConfFolder`, the interface name, and the configured extension. With defaults, the example above uses:

```text
./conf/app/AppConf.hotconf
```

If a configuration is missing, the factory writes a default configuration from the interface methods and annotations.

## Configuration Annotations

| Annotation                   | Target              | Purpose                                                |
|------------------------------|---------------------|--------------------------------------------------------|
| `@ConfFolder("folder")`      | interface           | Stores the configuration under a folder.               |
| `@ConfDoc("text")`           | interface or method | Writes comments for the config or parameter.           |
| `@ConfDefaultValue("value")` | method              | Provides the initial value if the parameter is absent. |

Only no-argument methods are supported as configuration properties.

## Value Parsing

Configuration values are stored as strings and converted to the Java return type of each interface method.

Supported target types include:

- `String`
- `boolean` / `Boolean`
- `byte` / `Byte`
- `short` / `Short`
- `int` / `Integer`
- `long` / `Long`
- `float` / `Float`
- `double` / `Double`
- `BigDecimal`
- `BigInteger`
- `char` / `Character`

### Dynamic Environment Values

Values may include environment placeholders:

```text
$ENV{NAME}
```

They are resolved through `DynamicParams.env("NAME")` before type conversion.

Example:

```text
port=$ENV{APP_PORT}
workers=$ENV{WORKER_COUNT} * 2
```

### String Escapes

Standard substitutions are supported:

| Input | Result |
| --- | --- |
| `\n` | newline |
| `\t` | tab |
| `\r` | carriage return |
| `\b` | backspace |
| `\f` | form feed |
| `\\` | backslash |
| `\"` | double quote |
| `\'` | single quote |

For numeric values, whitespace, `_`, and these separator/control characters are ignored where applicable.

### Numeric Formatting

Numeric parsing accepts:

- spaces as separators: `1 234`
- underscores as separators: `1_234`
- comma decimal separator: `12,5`
- negative values: `-12.5`
- exponent notation: `1.3e+4`, `0.456e-17`, `-17.45E+61`
- hexadecimal integers: `0x234`, `0xAfeE76a03`, `0X38`
- binary integers: `0b100110101`, `0B0011010110`
- octal integers: `0o16542`, `0O643`

Leading-zero numbers without a base prefix remain decimal:

```text
00034 == 34
01896 == 1896
```

### Numeric Expressions

Numeric target types may use expressions:

```text
1 + 2
17 - 3
10 * 34
450 / 10
3 + 7*3
$ENV{NAME} * 16
(23 + 4)*(100 - 45)
```

Supported operators:

- addition: `+`
- subtraction: `-`
- multiplication: `*`
- division: `/`
- parentheses: `(...)`

Operator priority follows normal arithmetic rules. Signs after `e` or `E` are treated as part of exponent notation; otherwise `+` and `-` are expression operators.

For integer target types:

- integer-only expressions use integer arithmetic
- expressions with decimal syntax or exponent notation use `BigDecimal`, then round to the nearest whole number

For floating-point target types:

- expressions are evaluated as `BigDecimal` with 50 significant digits, then converted to `float` or `double`

For boolean target types:

- known textual values such as `true`, `yes`, `on`, `false`, `no`, `off` are supported
- numeric expressions evaluate to `false` when integer result is `0`
- floating-point expressions evaluate to `false` when `abs(value) < 0.001`

## File Storage

`ConfTunnelFile` stores one configuration as a line-oriented UTF-8 file:

```text
#Application configuration

#HTTP port
port=8080

enabled=true
host=localhost
```

The tunnel creates parent directories as needed and uses the file modification time as the modification marker.

## JDBC Storage

`kz-pompei-conf-jdbc` stores configurations in a database table. The builder detects PostgreSQL or MariaDB from JDBC metadata:

```java
import kz.pompei.conf.jdbc.ConfTunnelJdbc;
import kz.pompei.conf.jdbc.ConfTunnelJdbcBuilder;
import kz.pompei.conf.jdbc.ConfTunnelJdbcDef;

ConfTunnelJdbcDef def = new ConfTunnelJdbcDef();
def.tableName = "conf";

ConfTunnelJdbc tunnel = ConfTunnelJdbcBuilder.build(connectionGet, def);
```

The table and schema are created automatically when missing. Column names are configurable through `ConfTunnelJdbcDef`.

## etcd Storage

`kz-pompei-conf-etcd` stores each configuration under one etcd key:

```java
import io.etcd.jetcd.Client;
import kz.pompei.conf.etcd.ConfTunnelEtcd;
import kz.pompei.conf.etcd.ConfTunnelEtcdDef;

ConfTunnelEtcdDef def = new ConfTunnelEtcdDef();
def.keyPrefix = "/kz-pompei-conf-etcd/";

try (Client client = Client.builder().endpoints("http://localhost:17403").build();
     ConfTunnelEtcd tunnel = new ConfTunnelEtcd(client, def)) {
  // use tunnel with HotConfFactory
}
```

The etcd tunnel uses the key `modRevision` as the modification marker.

## Development Notes

- Source directories are `src` and `test_src`, not Gradle defaults.
- Tests use TestNG and AssertJ.
- The project uses Lombok and JetBrains annotations.
- Java toolchain is configured for Java 21.
- Project group is `kz.pompei.conf`.

## License

No license file is currently included in this repository. Add one before publishing if you want to define reuse terms for GitHub users.
