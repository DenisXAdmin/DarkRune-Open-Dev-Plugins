# Changelog — DarkRune Tab

All notable changes to DarkRune Tab are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [1.0-RELEASE] - 2026-09-13

### Added (from 1.1-SNAPSHOT)
- **Chat module** (`chat`) — flexible chat formatting: permission-based
  formats with priorities, default fallback format, and control over
  colors in player messages (disabled by default).
- **Full control over the tab entry string** (`modules.tablist.player_format`) —
  the name in the tab list is composed from placeholders: nicknames, HP,
  titles, hidden names.
- **TPS placeholders**: `%server_tps%`, `%server_tps_5%`, `%server_tps_15%`.
- **Instant LuckPerms updates** — prefix/suffix/group changes are applied
  immediately (event-based cache invalidation), without waiting for TTL.
- **TaskHandle API** — modules can cancel and restart periodic tasks on reload.

### Changed (from 1.1-SNAPSHOT)
- `/darkrunetab reload` — applies changes instantly, syncs module state
  and restarts periodic tasks with the new intervals.
- `/darkrunetab toggle` — resets visible effects on disable and applies
  them immediately on enable.
- **LuckPerms and PlaceholderAPI are now optional** — the plugin starts
  without them with reduced functionality.
- **Chat module disabled by default** — enable manually via
  `modules.chat.enabled: true` (plugin doesn't touch chat without admin consent).

### Fixed (from 1.1-SNAPSHOT)
- Folia scheduler signatures.
- Version in `plugin.yml` not substituted (resource filtering enabled).
- `config.yml` not packaged into JAR.
- Placeholder cache no longer serves stale LuckPerms prefixes/suffixes.

### Fixed (new in 1.0-RELEASE)
- **Critical**: Plugin crash on Folia without LuckPerms installed
  (`NoClassDefFoundError: net/luckperms/api/LuckPermsProvider`).
  Now checks for API availability before loading integration classes.
- **Critical**: Plugin crash on Folia with scoreboard modules
  (`UnsupportedOperationException` from `getNewScoreboard()`).
  Scoreboard now lazily initializes and gracefully degrades on Folia.
- NullPointerException in `/darkrunetab stats` when LuckPerms unavailable.

### Stability
- All modules now check platform capabilities before initialization.
- Improved error handling for optional dependencies.
- Graceful degradation when scoreboard is unsupported (Folia).

## [1.1-SNAPSHOT] - 2026-08-27

Pre-release with chat module, instant LuckPerms updates, and various fixes.

## [1.0-SNAPSHOT] - 2026-08-26

Initial pre-release.
