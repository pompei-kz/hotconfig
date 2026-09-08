# Changelog

All notable changes to this project are documented here.

This project keeps short release summaries in this file and detailed notes in
`versions/<version>.md`.

## [0.0.10] - 2026-09-08

### Added

- Added the change of the type of the PostgreSQL timestamp columns to `TIMESTAMPTZ`: the types of `colCreatedAt` and
  `colLastModified` are checked every time the table is checked - just as the column of the row order and the trigger are - and a
  column which keeps a timestamp without a time zone is brought to `timestamp with time zone`, with the values of the rows kept as
  they were.
- Added the check of the type of a column through `information_schema.columns.data_type`. Only a column of the type
  `timestamp without time zone` is changed, and nothing is changed when both columns already keep the time zone, so a database which
  is already prepared needs no rights for DDL at all.
- Added the handling of the race of several instances which change the type at the same moment: the loser of the race swallows its
  error only when the column is of the wanted type anyway.
- Added JDBC tests: a fresh table keeps the time zone; a table of an older version is brought to `TIMESTAMPTZ` with the values of the
  rows kept - the same wall clock and the same moment read by `modificationMarker`; the same for a schema-qualified table name; and
  eight instances racing for `ALTER COLUMN ... TYPE TIMESTAMPTZ`.
- Added test helpers `JdbcTestDbUtils.columnDataType(...)` and `timestampAsText(...)`.

### Changed

- The PostgreSQL `CREATE TABLE` now creates `colCreatedAt` and `colLastModified` as `TIMESTAMPTZ` instead of `TIMESTAMP`, so a plain
  wall clock without a time zone is never stored again.
- MariaDB keeps the path it had before: its own `TIMESTAMP` is stored in UTC and shown in the time zone of the session, so it already
  keeps the moment itself.

### Migration Notes

- Nothing has to be done by hand - the type is changed when the table is checked, once per tunnel instance.
- The database user needs the right to `ALTER` the table only until the columns are changed; after that no DDL is run at all.
- `ALTER COLUMN ... TYPE` rewrites the table under an `ACCESS EXCLUSIVE` lock. The configuration table is small, but this happens on
  the first start of an application of this version.

## [0.0.9] - 2026-09-07

### Added

- Added `ConfigTunnelJdbcBuilder.triggerName(...)` / `triggerName()` for the trigger name template, defaulting to `%__trigger`, where
  the percent sign (`%`) means the table name.
- Added `ConfigTunnelJdbcBuilder.triggerFnName(...)` / `triggerFnName()` for the trigger function name template, defaulting to
  `%__trigger_fn`. PostgreSQL only - MariaDB creates the trigger without a separate function.
- Added `ConfigTunnelJdbc.Def.triggerName` and `ConfigTunnelJdbc.Def.triggerFnName`.
- Added `ConfigTunnelJdbc.tableExists(...)` and `ConfigTunnelJdbc.triggerExists(...)`, so the table, the trigger and the trigger
  function are checked one by one and created only when they are really missing.
- Added `ConfigTunnelJdbc.schemaExists(...)`, so the schema is also created only when it is really missing, and its race is handled
  at the level of the schema - an error swallowed above it would skip the creation of the table.
- Added `ConfigTunnelJdbc.createTableIfMissing(...)`, `isDuplicateObject(...)` and `isMadeByOther(...)` for the race of several
  instances which start at the same moment: the loser of the race swallows its error only when the wanted schema, table, column,
  trigger or function is there anyway.
- Added JDBC tests for the races of eight instances starting at once - for the table, the schema, the order column and the
  trigger - and for the classification of the errors of a lost race.
- Added a `BEFORE UPDATE` trigger to MariaDB table creation; before this version MariaDB relied on `ON UPDATE CURRENT_TIMESTAMP` only.
- Added builder validation rejecting empty trigger templates and templates containing a period.
- Added JDBC tests covering trigger behavior, custom trigger name templates and schema-qualified table names.

### Changed

- The PostgreSQL trigger now refreshes `colLastModified` of the configuration row - the row where `colParamName` is empty - when a
  parameter row changes, so a parameter edited directly in the database is seen as a change of the whole configuration file.
- PostgreSQL trigger and function names now come from the builder templates instead of the hardcoded
  `trg_<table>_last_modified` / `fn_<table>_last_modified`.
- The table is created only when `tableExists(...)` says it is missing, so `CREATE TABLE IF NOT EXISTS` is no longer sent to an
  existing table. An already prepared database needs no rights for DDL at all.
- The trigger is no longer dropped and created again on every tunnel instance. It is created only when it is missing, and its body
  is replaced only when it differs from the body of this version, so there is no moment when changes of the table are not caught
  by anybody.
- `ConfigTunnelJdbc` resolves the schema and the table name through the new `schemaName()` / `simpleTableName()` helpers.
- README documents the trigger, the name templates and the MariaDB limitation.

## [0.0.8] - 2026-08-08

### Added

- Added `ConfigTunnelJdbc.Def.colOrder` and `ConfigTunnelJdbcBuilder.colOrder(...)` / `colOrder()` for the parameter order column,
  defaulting to `order_index`.
- Added `order_index INT NOT NULL DEFAULT 0` to PostgreSQL and MariaDB table creation, with an automatic `ALTER TABLE ... ADD COLUMN`
  when an existing table does not have the column yet.
- Added JDBC tests covering explicit parameter ordering.

### Changed

- JDBC writes store the configuration row with order `0` and parameters with orders `1..n`, and reads sort by the order column first,
  so parameter order survives a write/read round trip.
- `ConfigTunnelJdbc` runs table and schema DDL only once per tunnel instance instead of on every write, notice write and modification
  marker read.
- Published POM `url` for all three modules now points to `https://pompei.kz/program/kz-pompei-hotconfig`.

## [0.0.7] - 2026-05-25

### Added

- Added `ConfigTunnelEtcdBuilder` and `ConfigTunnelEtcd.builder()` as the public etcd tunnel construction API.
- Added `ConfigTunnelJdbcBuilder` and `ConfigTunnelJdbc.builder()` as the public JDBC tunnel construction API.
- Added `ConfigTunnelEtcdBuilder.errorPrefix(...)` for configuring the etcd tunnel parameter error marker text.
- Added `ConfigTunnelFileBuilder.errorPrefix(...)` for configuring the file tunnel parameter error marker text.
- Added `ConfigTunnelMem` tests covering read/write behavior, modification markers, notices, copy isolation, and path isolation.

### Changed

- File tunnel parameter error prefix configuration now lives in `ConfigTunnelFile.Def.errorPrefix`, defaulting to `ERROR `.
- Etcd tunnel parameter error prefix configuration now lives in `ConfigTunnelEtcd.Def.errorPrefix`, defaulting to `ERROR `.
- JDBC tunnel table and column configuration now lives in `ConfigTunnelJdbcBuilder` and `ConfigTunnelJdbc.Def`.
- File and etcd tunnels now write multiline parameter errors as several consecutive prefixed lines instead of escaping newlines into one line.
- File and etcd parameter error text is stored raw per line; backslashes are no longer escaped or unescaped for error text.
- File and etcd `writeNoticeLines` now delete notice storage when passed an empty list.
- Etcd tunnel storage calls now live directly in `ConfigTunnelEtcd` instead of a separate internal `EtcdStorage` abstraction.
- README dependency snippets and release links now point to `0.0.7`.

### Removed

- Removed `ConfTunnelEtcdDef`; configure etcd tunnels through `ConfigTunnelEtcd.builder()` instead.
- Removed `ConfigTunnelJdbcDef`; configure JDBC tunnels through `ConfigTunnelJdbc.builder()` instead.

## [0.0.6] - 2026-05-24

### Added

- Added `HotConfigFactoryBuilder` as the public construction API for `HotConfigFactory`.
- Added separate `Clock` and `EnvSrc` configuration hooks for refresh timing and environment substitution.
- Added `ConfParam.error` persistence for file, JDBC, and etcd tunnels.
- Added JDBC `error` text column support through `ConfigTunnelJdbcBuilder.colError(...)`.
- Added public API Javadocs for factory and builder classes.

### Changed

- README dependency snippets and release links now point to `0.0.6`.
- File and etcd tunnels write parameter errors after the `name=value` line using the `#ERROR ` prefix.
- File and etcd tunnels escape newlines and backslashes in parameter errors and restore them on read.
- Tests now use focused `ClockFake` and `EnvSrcFake` test utilities instead of the removed combined dynamic source.

### Removed

- Removed `HotConfigFactoryParams` and `HotConfigFactoryParamsBuilder`.
- Removed the legacy `DynamicParams` API and `DynamicParamsFake`.

### Migration Notes

- Construct factories with `HotConfigFactory.builder().tunnel(...).build()`.
- If you already have JDBC config tables, add the error column manually. With default settings, the column is:

  ```sql
  error TEXT
  ```

## [0.0.5] - 2026-05-22

### Added

- Added notice text storage for all `ConfigTunnel` backends.
- Added JDBC `notice` text column support for configuration-level notice lines.
- Added etcd notice keys with a configurable notice extension.
- Added tests for notice read/write behavior in file, JDBC, and etcd tunnels.

### Changed

- `ConfigTunnelFile` now reads and writes notice files at `localPath + noticeExtension`.
- JDBC stores notice text only on the configuration row; parameter rows keep notice data null.
- `ConfigTunnelFile` tests now use the builder-based construction path.

### Migration Notes

- Custom `ConfigTunnel` implementations must support `readNoticeLines` and `writeNoticeLines`.
- Existing JDBC tables need a manual `TEXT` column matching `ConfigTunnelJdbcBuilder.colNotice(...)`
  before notice storage is used.
- `ConfigTunnelFile` should be created through `ConfigTunnelFile.builder()` with both
  `baseDir` and `noticeExtension` set.

[0.0.10]: versions/0.0.10.md
[0.0.9]: versions/0.0.9.md
[0.0.8]: versions/0.0.8.md
[0.0.7]: versions/0.0.7.md
[0.0.6]: versions/0.0.6.md
[0.0.5]: versions/0.0.5.md
