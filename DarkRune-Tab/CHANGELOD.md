# Changelog — DarkRune Tab

All notable changes to DarkRune Tab are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [1.1-SNAPSHOT] - 2026-08-27

### Added
- Chat formatting module (`chat`): permission-based formats with
  priorities, default fallback format and player color control.
- Full placeholder-driven player name in the tab list
  (`modules.tablist.player_format`) — nicknames, HP, titles, hidden names.
- TPS placeholders: `%server_tps%`, `%server_tps_5%`, `%server_tps_15%`.
- `TaskHandle` API so modules can cancel and restart their periodic tasks.

### Changed
- LuckPerms prefix/suffix updates are now instant (event-based cache
  invalidation) instead of waiting for cache TTL.
- `/darkrunetab reload` now applies changes instantly, syncs module
  enabled-state and restarts periodic tasks with new intervals.
- `/darkrunetab toggle` now resets visible effects on disable and applies
  immediately on enable.
- LuckPerms and PlaceholderAPI are now optional (softdepend); the plugin
  starts without them with reduced functionality.

### Fixed
- Folia `runAtFixedRate` / `runDelayed` scheduler signatures (ticks, not TimeUnit).
- `plugin.yml` version placeholder not substituted (enabled resource filtering).
- `config.yml` not packaged into the JAR.
- Placeholder cache no longer serves stale LuckPerms prefixes/suffixes.

## [1.0-SNAPSHOT] - 2026-08-26

### Added
- Initial pre-release.
- TabList module (header/footer with MiniMessage).
- Nametags module (prefix/suffix via scoreboard teams).
- Scoreboard module (anti-flicker side panel).
- Native Paper and Folia platform adapters.
- Deep LuckPerms integration (prefixes, suffixes, group weight sorting).
- PlaceholderAPI support + built-in placeholders.
- Smart caching (Caffeine) with per-placeholder TTL.
- Commands: help, reload, toggle, debug, stats.