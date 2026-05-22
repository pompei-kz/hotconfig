# Changelog

All notable changes to this project are documented here.

This project keeps short release summaries in this file and detailed notes in
`versions/<version>.md`.

## [0.0.4] - 2026-05-22

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

[0.0.4]: versions/0.0.4.md
