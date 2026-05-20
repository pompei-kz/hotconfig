# kz-pompei-hotconfig

> Hot configuration for Java services, backed by files, JDBC, or etcd.

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](#requirements)
[![Gradle](https://img.shields.io/badge/build-Gradle-green.svg)](#build-and-test)
[![TestNG](https://img.shields.io/badge/tests-TestNG-orange.svg)](#build-and-test)
[![Version](https://img.shields.io/badge/version-0.0.3-lightgrey.svg)](versions/version.txt)
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
- [Build And Test](#build-and-test)
- [Project Layout](#project-layout)
- [License](#license)

## Quick Start

This example mirrors the configuration pattern tested in `HotConfigFactoryTest`, but uses `ConfigTunnelFile` as the storage tunnel.

### 1. Define A Configuration Interface

```java
import ann.kz.pompei.hotconfig.core.ConfDefaultValue;
import ann.kz.pompei.hotconfig.core.ConfDoc;
import ann.kz.pompei.hotconfig.core.ConfFolder;

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

### 2. Create A Factory Backed By Files

```java
import java.nio.file.Path;
import kz.pompei.hotconfig.core.ConfigTunnelFile;
import kz.pompei.hotconfig.core.DynamicParams;
import kz.pompei.hotconfig.core.HotConfigFactory;
import kz.pompei.hotconfig.core.HotConfigFactoryParams;

Path baseDir = Path.of("/path/to/config/root");

HotConfigFactoryParams params = HotConfigFactoryParams.builder()
                                                      .extension(".hot")
                                                      .build();

HotConfFactory factory = new HotConfFactory(new ConfTunnelFile(baseDir), params);

TestConf1 config = factory.createConf(TestConf1.class);

String param1     = config.param1();     // "def value 1"
int    accessPort = config.accessPort(); // 1044
```

### 3. Inspect The Generated File

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

### 4. Edit and read new values

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
  implementation 'kz.pompei.hotconfig:kz-pompei-hotconfig-core:0.0.3'

  // Optional storage backends
  implementation 'kz.pompei.hotconfig:kz-pompei-hotconfig-jdbc:0.0.3'
  implementation 'kz.pompei.hotconfig:kz-pompei-hotconfig-etcd:0.0.3'
}
```

### Maven

```xml
<dependencies>
  <dependency>
    <groupId>kz.pompei.hotconfig</groupId>
    <artifactId>kz-pompei-hotconfig-core</artifactId>
    <version>0.0.3</version>
  </dependency>

  <!-- Optional storage backends -->
  <dependency>
    <groupId>kz.pompei.hotconfig</groupId>
    <artifactId>kz-pompei-hotconfig-jdbc</artifactId>
    <version>0.0.3</version>
  </dependency>
  <dependency>
    <groupId>kz.pompei.hotconfig</groupId>
    <artifactId>kz-pompei-hotconfig-etcd</artifactId>
    <version>0.0.3</version>
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

`ConfTunnelFile` stores each configuration as a UTF-8 text file.

```java
HotConfigFactory factory = new HotConfigFactory(
  new ConfTunnelFile(Path.of("/path/to/config/root"))
);
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

### JDBC

`kz-pompei-conf-jdbc` stores configurations in a database table. PostgreSQL and MariaDB are detected from JDBC metadata.

```java
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbc;
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbcBuilder;
import kz.pompei.hotconfig.jdbc.ConfigTunnelJdbcDef;

ConfigTunnelJdbcDef def = new ConfigTunnelJdbcDef();
def.tableName = "hitconfig";

ConfTunnelJdbc tunnel = ConfigTunnelJdbcBuilder.build(() -> dataSource.getConnection(), def);
```

The table and schema are created automatically when missing. Column names are configurable through `ConfigTunnelJdbcDef`.

### etcd

`kz-pompei-conf-etcd` stores each configuration under one etcd key.

```java
import io.etcd.jetcd.Client;
import kz.pompei.hotconfig.etcd.ConfigTunnelEtcd;
import kz.pompei.hotconfig.etcd.ConfigTunnelEtcdDef;

ConfigTunnelEtcdDef def = new ConfigTunnelEtcdDef();
def.keyPrefix = "/kz-pompei-conf-etcd/";

try (Client client = Client.builder().endpoints("http://localhost:17403").build();
     ConfigTunnelEtcd tunnel = new ConfigTunnelEtcd(client, def)) {
  HotConfFactory factory = new HotConfFactory(tunnel);
}
```

The etcd tunnel uses the key `modRevision` as the modification marker.

## Build And Test

Requirements:

- JDK 21
- Gradle wrapper is included in this repository
- Docker for JDBC and etcd integration tests

Run core tests:

```bash
./gradlew :kz-pompei-conf-core:test
```

Start local integration services:

```bash
bash docker/docker-recreate.bash
```

Then run backend tests:

```bash
./gradlew :kz-pompei-conf-jdbc:test
./gradlew :kz-pompei-conf-etcd:test
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
