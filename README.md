# kz-pompei-hotconfig

> Hot configuration for Java services, backed by files, JDBC, or etcd.

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](#requirements)
[![Gradle](https://img.shields.io/badge/build-Gradle-green.svg)](#build-and-test)
[![TestNG](https://img.shields.io/badge/tests-TestNG-orange.svg)](#build-and-test)
[![Version](https://img.shields.io/badge/version-0.0.12-lightgrey.svg)](versions/version.txt)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

`kz-pompei-hotconfig` turns a plain Java interface into a live configuration object.
The first read creates a configuration file or storage row from defaults,
later reads return typed values, and refresh checks pick up changes from the backing storage.

```java
AppConfigInterface conf = factory.createConf(AppConfigInterface.class);

int port = conf.port(); // read from config file (or SQL DB, or etcd)
boolean enabled = conf.enabled();
```

## Contents

- [Quick Start](#quick-start)
- [Features](#features)
- [Modules](#modules)
- [Installation](#installation)
- [Configuration Interfaces](#configuration-interfaces)
- [Value Parsing](#value-parsing)
- [Storage Backends](#storage-backends)
- [Changelog](#changelog)
- [Build And Test](#build-and-test)
- [Project Layout](#project-layout)
- [License](#license)

## Quick Start

This example mirrors the configuration pattern tested in `HotConfigFactoryTest`, but uses `ConfigTunnelFile` as the storage tunnel.

### 1. Dependency

Add this to build.gradle / dependencies

```groovy
implementation "kz.pompei.hotconfig:kz-pompei-hotconfig-core:0.0.12"
```

### 2. Define A Configuration Interface

```java
import kz.pompei.hotconfig.core.ann.ConfDefaultValue;
import kz.pompei.hotconfig.core.ann.ConfDoc;
import kz.pompei.hotconfig.core.ann.ConfFolder;

@ConfDoc("about1\nabout2\nabout3")
@ConfFolder("cool/folder")
public interface TestConf1 {

  @ConfDoc("description1\ndescription2")
  @ConfDefaultValue("def value 1")
  String param1();

  @ConfDoc("description3\ndescription4\ndescription5")
  @ConfDefaultValue("20 + 1024")
  int accessPort();
}
```

### 3. Create A Factory Backed By Files

```java
import java.nio.file.Path;
import kz.pompei.hotconfig.core.ConfigTunnelFile;
import kz.pompei.hotconfig.core.HotConfigFactory;

Path baseDir = Path.of("/path/to/config/root");

var tunnel = ConfigTunnelFile.builder()
                             .baseDir(baseDir)
                             .noticeExtension(".notice")
                             .build();

HotConfigFactory factory = HotConfigFactory.builder()
                                           .tunnel(tunnel)
                                           .extension(".hot")
                                           .build();

TestConf1 config = factory.createConf(TestConf1.class);

String param1     = config.param1();     // "def value 1"
int    accessPort = config.accessPort(); // 1044
```

### 4. Inspect The Generated File

On the first read, the library creates:

```text
/path/to/config/root/cool/folder/TestConf1.hot
```

With content generated from annotations:

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
accessPort=20 + 1024
```

### 5. Edit and read new values

Edit the file manually:

```text
param1=SKY TREE
accessPort=20 + 1024 + 700
```

Then read new values without restart application

```java

String updatedParam1 = config.param1();     // "SKY TREE"
int    accessPort    = config.accessPort(); // 1744
```

## Features

- Typed configuration access through Java interfaces.
- Automatic default configuration creation (You don't need to create configuration files manually – just change them from the default values).
- Automatic supplementation when new interface methods are added.
- Hot refresh through storage-specific modification markers.
- You can store configuration in files, or SQL DB, or etcd, or write your own implementation of the tunnel interface (ConfigTunnel).
- Comments generated from annotations.
- Notice lines associated with a whole configuration can be stored separately from parameter comments.
- Rich string-to-type conversion for primitives, boxed types, `BigDecimal`, `BigInteger`, `String`, and `char`.
- Numeric expression evaluation with normal math precedence.
- `$ENV{NAME}` substitution from environment variables.

## Modules

| Module                     | Description                                                            |
|----------------------------|------------------------------------------------------------------------|
| `kz-pompei-hotconfig-core` | Core API, proxy factory, file tunnel, annotations, parser, and models. |
| `kz-pompei-hotconfig-jdbc` | JDBC tunnel for PostgreSQL and MariaDB.                                |
| `kz-pompei-hotconfig-etcd` | etcd v3 tunnel implemented with jetcd.                                 |

## Installation

The library is published to Maven Central. It requires Java 21.

Use `kz-pompei-hotconfig-core` for file-backed configuration. Add `kz-pompei-hotconfig-jdbc` or `kz-pompei-hotconfig-etcd` when you need those storage backends.

### Gradle

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation "kz.pompei.hotconfig:kz-pompei-hotconfig-core:0.0.12"
  // If you store configs in files, no extra modules are needed

  // Optional storage backends

  // Add this if you want to store configs in SQL database
  implementation "kz.pompei.hotconfig:kz-pompei-hotconfig-jdbc:0.0.12"
  
  // Add this if you want to store configs in etcd database
  implementation "kz.pompei.hotconfig:kz-pompei-hotconfig-etcd:0.0.12"
}
```

### Maven

```xml
<dependencies>
  <dependency>
    <groupId>kz.pompei.hotconfig</groupId>
    <artifactId>kz-pompei-hotconfig-core</artifactId>
    <version>0.0.12</version>
  </dependency>

  <!-- If you store configs in files, no extra modules are needed -->

  <!-- Optional storage backends -->

  <!-- Add this if you want to store configs in SQL database -->
  <dependency>
    <groupId>kz.pompei.hotconfig</groupId>
    <artifactId>kz-pompei-hotconfig-jdbc</artifactId>
    <version>0.0.12</version>
  </dependency>

  <!-- Add this if you want to store configs in etcd database -->
  <dependency>
    <groupId>kz.pompei.hotconfig</groupId>
    <artifactId>kz-pompei-hotconfig-etcd</artifactId>
    <version>0.0.12</version>
  </dependency>
</dependencies>
```

## Configuration Interfaces

A configuration interface is a plain Java interface with no-argument methods. Each method name becomes a parameter name.

```java
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

Annotations:

| Annotation                   | Target              | Purpose                                               |
|------------------------------|---------------------|-------------------------------------------------------|
| `@ConfFolder("folder")`      | interface           | Places the configuration under a folder.              |
| `@ConfDoc("text")`           | interface or method | Writes comments into generated configuration storage. |
| `@ConfDefaultValue("value")` | method              | Defines the value used when the parameter is absent.  |

Only zero-argument methods are supported.

## Value Parsing

Configuration values are stored as strings. `ParseUtil` converts them to the Java return type of the interface method.

Supported target types:

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

### Environment Substitution

Use `$ENV{NAME}` to resolve environment variables before type conversion:

```text
port=$ENV{APP_PORT}
workers=$ENV{WORKER_COUNT} * 2
```
### String Escapes

Supported escape sequences:

| Input   | Result          |
|---------|-----------------|
| `\n`    | newline         |
| `\t`    | tab             |
| `\r`    | carriage return |
| `\b`    | backspace       |
| `\f`    | form feed       |
| `\\`    | backslash       |
| `\"`    | double quote    |
| `\'`    | single quote    |

For numeric targets, whitespace, `_`, backslash separators, and escaped control characters are ignored where applicable.

### Numbers

Numeric parsing accepts:

```text
1 234
1_234
12,5
-12.5
1.3e+4
0.456e-17
-17.45E+61
0xAfeE76a03
0b100110101  - binary
0o16542      - octal
```

Base-prefixed integer literals:

| Prefix      | Base        | Example                       |
|-------------|-------------|-------------------------------|
| `0x` / `0X` | hexadecimal | `0x234`, `0xAfeE76a03`        |
| `0b` / `0B` | binary      | `0b100110101`, `0B0011010110` |
| `0o` / `0O` | octal       | `0o16542`, `0O643`            |

Leading-zero numbers without a base prefix stay decimal:

```text
00034 == 34
01896 == 1896
```

### Expressions

Numeric target types can evaluate expressions:

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

- `+`
- `-`
- `*`
- `/`
- parentheses

Precedence follows normal mathematics. A `+` or `-` after `e` or `E` is part of exponent notation; elsewhere it is an operator.

Integer targets:

- integer-only expressions use integer arithmetic
- decimal or exponent expressions use `BigDecimal`, then round to the nearest whole number

Floating-point targets:

- expressions use `BigDecimal` with 50 significant digits
- final value is converted to `float` or `double`

Boolean targets:

- text values such as `true`, `yes`, `on`, `false`, `no`, and `off` are supported
- integer expression result `0` is `false`, otherwise `true`
- floating expression result with `abs(value) < 0.001` is `false`, otherwise `true`

## Storage Backends

### Files

`ConfigTunnelFile` stores each configuration as a UTF-8 text file.

```java
ConfigTunnelFile tunnel = ConfigTunnelFile.builder()
  .baseDir(Path.of("/path/to/config/root"))
  .noticeExtension(".notice")
  .build();

HotConfigFactory factory = HotConfigFactory.builder()
  .tunnel(tunnel)
  .build();
```

Example generated file:

```text
#Application configuration

#HTTP port
port=8080

enabled=true
host=localhost
```

The file tunnel uses file modification time as the modification marker.
Notice lines are stored in a sibling UTF-8 file at `localPath + noticeExtension`.

### JDBC

`kz-pompei-hotconfig-jdbc` stores configurations in a database table. PostgreSQL and MariaDB are detected from JDBC metadata.

```java
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbc;

ConfigTunnelJdbc tunnel = ConfigTunnelJdbc.builder()
                                          .connectionGet(() -> dataSource.getConnection())
                                          .tableName("hotconfig")
                                          .build();
```

The table and schema are created automatically when missing. Column names are configurable through `ConfigTunnelJdbcBuilder`.
Parameter order is stored in `colOrder`, defaulting to the `order_index` integer column.
Notice lines are stored in the `colNotice` text column on the configuration row only. If you already have a table from an older version,
add this column manually before using notice storage.
Parameter errors are stored in the `colError` text column on parameter rows. Existing JDBC tables must add this column manually before
using version 0.0.7.

The schema, the table, the trigger and, in PostgreSQL, the trigger function are checked separately, and every one of them is
created only when it is really missing: an existing table may have no trigger, because the table could be created by an older
version or by hand, and the trigger could be dropped later. When everything is in place no DDL is run at all, so a database user
with rights to read and write the data of an existing table is enough.

Several instances of an application may start at the same moment, and then they race for the same table: all of them see no table
and all of them create it. The check and the creation cannot be made one atomic step, so the loser of the race gets an error
instead of the table. Such an error is swallowed only when the wanted schema, table, column, trigger or function is there anyway,
made by the winner of the race; every other error goes up as it is.

The value which is already in the database is the value that counts. `write(...)` only brings the rows which are not in the table
yet: a row which is already there is left exactly as it is, and no row is ever deleted. A parameter which the code knows and the
database does not gets its default written into the table - this is what writing is for - while a value edited in the database is
never overwritten by the default from the code. The other side of the same rule: a comment changed in the code does not reach a row
which is already there, an error found in a value is not written into the row of that value, and a parameter removed from the code
keeps its row in the table until somebody deletes it by hand.

Because of this several instances may write the same configuration at the same moment without waiting for each other - which is
what a first start on a new database looks like, where every instance writes back the same defaults at once. Whoever inserts a row
first owns it, the others leave it alone, and no instance falls with a violation of the primary key. Two instances writing
different values of one parameter at once leave one of those values in the table - whichever came first - and never a value that
nobody wrote.

In PostgreSQL `colCreatedAt` and `colLastModified` are of the type `TIMESTAMPTZ` - `timestamp with time zone` - so the value keeps the
moment itself and is read as the very same moment by an application in any time zone. A table made by an older version of this library
keeps these columns as a plain `TIMESTAMP` - a wall clock without any time zone, which two applications in two time zones read as two
different moments. The type of both columns is checked every time the table is checked, and such a column is brought to `TIMESTAMPTZ`
automatically, with the values of the rows kept as they were. A column of any other type is left alone, and when both columns already
keep the time zone no DDL is run at all. In MariaDB these columns stay `TIMESTAMP`: MariaDB stores such a value in UTC and shows it in
the time zone of the session, so it already keeps the moment itself - the type without a time zone there is `DATETIME`, and this
library has never used it.

The trigger refreshes `colLastModified` of the changed row, and in PostgreSQL it refreshes `colLastModified` of the configuration
row as well - the row where `colParamName` is empty. Because of this, a change of a single parameter value made directly in the
database is seen as a change of the whole configuration file. Trigger names are configurable through `triggerName(...)` and
`triggerFnName(...)`; the percent sign (`%`) in these templates is replaced with the table name:

```java
ConfigTunnelJdbc tunnel = ConfigTunnelJdbc.builder()
                                          .connectionGet(() -> dataSource.getConnection())
                                          .tableName("hotconfig")
                                          .triggerName("%__trigger")
                                          .triggerFnName("%__trigger_fn")
                                          .build();
```

`triggerFnName` is used by PostgreSQL only - MariaDB creates the trigger without a separate function. In MariaDB the trigger also does
not touch the configuration row, because MariaDB forbids a trigger to change the table it is attached to (error 1442); there the change
of the whole configuration is seen through the maximum of `colLastModified` over all rows of the configuration, which is what
`modificationMarker` reads.

### etcd

`kz-pompei-hotconfig-etcd` stores each configuration under one etcd key.

```java
import io.etcd.jetcd.Client;
import kz.pompei.hotconfig.etcd.ConfigTunnelEtcd;

void someFunc() {
  // ...

  try (Client client = Client.builder().endpoints("http://localhost:17403").build();

       ConfigTunnelEtcd tunnel = ConfigTunnelEtcd.builder()
                                                 .client(client)
                                                 .keyPrefix("/kz-pompei-conf-etcd/")
                                                 .build()) {

    HotConfigFactory factory = HotConfigFactory.builder()
                                               .tunnel(tunnel)
                                               .build();
  }
  
  //...
}
```

The etcd tunnel uses the key `modRevision` as the modification marker.
Notice lines are stored under a separate key at `localPath + noticeExtension`.

### Notice Lines

`ConfigTunnel` supports notice text associated with a configuration:

```java
import java.util.List;

void someFunc() {
  // ...
  tunnel.writeNoticeLines("app/AppConf.hotconf", List.of("invalid port", "fallback was used"));
  List<String> notices = tunnel.readNoticeLines("app/AppConf.hotconf");
  // ...
}
```

Notice lines are separate from `@ConfDoc` comments and are not attached to individual parameters.
For file and etcd tunnels, writing an empty notice list deletes the existing notice file or key.

### Parameter Errors

`ConfParam.error` stores validation or parsing error text associated with a parameter.
File and etcd tunnels write it immediately after the `name=value` line using the `#ERROR ` prefix by default:

```text
port=bad value
#ERROR Cannot parse integer
#ERROR source=manual edit
```

File and etcd tunnels write multiline errors as consecutive prefixed lines and join them back with newline characters when read.
The configurable error prefix is the text after the mandatory leading `#`; for example, `ERROR ` is stored as `#ERROR `.
Backslashes in file and etcd error text are stored as-is. If `error` is null, the `#ERROR ` line is absent.
JDBC stores the same error text in the configurable `colError` text column.

## Changelog

Release notes are kept in [CHANGELOG.md](CHANGELOG.md). Detailed notes for this release are in
[versions/0.0.12.md](versions/0.0.12.md).

## Build And Test

Requirements:

- JDK 21
- Gradle wrapper is included in this repository
- Docker for JDBC and etcd integration tests

Run core tests:

```bash
./gradlew :kz-pompei-hotconfig-core:test
```

Start local integration services:

```bash
bash docker/docker-recreate.bash
```

Then run backend tests:

```bash
./gradlew :kz-pompei-hotconfig-jdbc:test
./gradlew :kz-pompei-hotconfig-etcd:test
```

Run all tests:

```bash
./gradlew test
```

## Project Layout

```text
kz-pompei-hotconfig
├── kz-pompei-hotconfig-core
├── kz-pompei-hotconfig-jdbc
├── kz-pompei-hotconfig-etcd
├── utils
├── docker
├── versions
└── build.gradle
```

Notes for contributors:

- Source directories are `src` and `test_src`.
- Tests use TestNG and AssertJ.
- Lombok and JetBrains annotations are used throughout the codebase.
- Java toolchain is configured for Java 21.

## Roadmap Ideas

- Add CI badges once a GitHub Actions workflow exists.
- Add more backend examples and production deployment recipes.

## License

This project is licensed under the [MIT License](LICENSE).
