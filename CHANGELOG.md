# Changelog

All notable changes to this project are documented here.

This project keeps short release summaries in this file and detailed notes in
`versions/<version>.md`.

## [0.0.6] - 2026-05-24

### Added

- Added `HotConfigFactoryBuilder` as the public construction API for `HotConfigFactory`.
- Added separate `Clock` and `EnvSrc` configuration hooks for refresh timing and environment substitution.
- Added `ConfParam.error` persistence for file, JDBC, and etcd tunnels.
- Added JDBC `error` text column support through `ConfigTunnelJdbcDef.colError`.
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
- Existing JDBC tables need a manual `TEXT` column matching `ConfigTunnelJdbcDef.colNotice`
  before notice storage is used.
- `ConfigTunnelFile` should be created through `ConfigTunnelFile.builder()` with both
  `baseDir` and `noticeExtension` set.

[0.0.6]: versions/0.0.6.md
[0.0.5]: versions/0.0.5.md
