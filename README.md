# DarkRune Tab

[![Pre-release](https://img.shields.io/badge/status-pre--release-orange)]()
[![License](https://img.shields.io/badge/license-DROL%20v1.0-blue)]()
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-yellow)]()
[![Folia](https://img.shields.io/badge/Folia-supported-green)]()
[![Java](https://img.shields.io/badge/Java-21-red)]()

High-performance **Tab List**, **Nametags** and **Scoreboard** plugin for
Paper 1.21+ servers with native Folia support.

> Русская версия: [README-RU.md](README-RU.md)

---

## Features

- **Tab List** — customizable header and footer with MiniMessage support
  (gradients, hover, click events)
- **Nametags** — prefixes and suffixes above the head and in the tab list
  via scoreboard teams
- **Scoreboard** — side panel with anti-flicker (only changed lines update)
- **Native Paper & Folia support** — platform adapters without conditional
  branching in hot paths
- **Deep LuckPerms integration** — prefixes, suffixes, group weights,
  player sorting by group weight
- **PlaceholderAPI** + built-in placeholders
- **Smart caching** — individual TTL per placeholder, LuckPerms metadata
  cache
- **Performance** — async placeholder resolution, update batching,
  virtual threads (Java 21)

---

## Requirements

| Component | Version | Required |
|---|---|---|
| Java | 21+ | Yes |
| Paper / Folia | 1.21+ | Yes |
| LuckPerms | 5.x | No (prefixes/sorting) |
| PlaceholderAPI | 2.x | No (external placeholders) |

---

## Installation

1. Download `DarkRuneTab-<version>.jar` from [Releases](../../releases)
2. Place it into the `plugins/` folder
3. Restart the server
4. Adjust `plugins/DarkRuneTab/config.yml`
5. Run `/darkrunetab reload`

---

## Building from source

```bash
git clone <repository-url>
cd DarkRune-Tab
mvn clean package
```

The final JAR appears at `target/DarkRuneTab-<version>.jar`.

---

## Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/darkrunetab help` | Show help | `darkrunetab.use` |
| `/darkrunetab reload` | Reload configurations | `darkrunetab.reload` |
| `/darkrunetab toggle <module>` | Enable/disable a module | `darkrunetab.toggle` |
| `/darkrunetab debug` | Debug information | `darkrunetab.debug` |
| `/darkrunetab stats` | Cache statistics | `darkrunetab.debug` |

Aliases: `/dtab`, `/tab`

---

## Modules

| Module | Description | Default |
|---|---|---|
| `tablist` | Tab list header and footer | Enabled |
| `nametags` | Prefixes/suffixes above the head | Enabled |
| `scoreboard` | Side panel | Disabled |

Toggle via `/darkrunetab toggle <module>` or in `config.yml`.

---

## Built-in placeholders

| Placeholder | Description |
|---|---|
| `%player_name%` | Player name |
| `%player_uuid%` | UUID |
| `%player_displayname%` | Display name |
| `%player_world%` | Current world |
| `%player_x% / %player_y% / %player_z%` | Coordinates |
| `%player_ping%` | Ping (ms) |
| `%player_health%` / `%player_health_max%` | Health |
| `%player_level%` / `%player_exp%` | Level / experience |
| `%player_food%` | Food level |
| `%player_gamemode%` | Game mode |
| `%server_online%` | Online players |
| `%server_max_players%` | Max players |
| `%server_name%` | Server name |
| `%server_tps%` | TPS (1 min) |
| `%server_tps_5%` / `%server_tps_15%` | TPS (5 / 15 min) |
| `%server_time%` | In-game time |
| `%stat_deaths%` / `%stat_jumps%` | Statistics |

### LuckPerms placeholders

| Placeholder | Description |
|---|---|
| `%display_lp_prefix%` | Prefix |
| `%display_lp_suffix%` | Suffix |
| `%display_lp_primary_group%` | Primary group |
| `%display_lp_primary_group_displayname%` | Group display name |
| `%display_lp_group_weight%` | Group weight |
| `%display_lp_group_color%` | Group color (meta `color`) |
| `%display_lp_meta_<key>%` | Custom meta value |
| `%display_lp_has_group_<name>%` | true/false |

---

## Configuration example

```yaml
modules:
  tablist:
    enabled: true
    header:
      text: |
        <gradient:gold:yellow>My Server</gradient>
        <gray>Online: <aqua>%server_online%</aqua>/<aqua>%server_max_players%</aqua>
      update_interval: 5
    footer:
      text: |
        <gray>TPS: <green>%server_tps%</green> | Ping: <yellow>%player_ping%ms</yellow>
      update_interval: 5
    player_format:
      format: "%display_lp_prefix%%player_name%%display_lp_suffix%"
    sorting:
      enabled: true
      type: "luckperms_weight"
      direction: "descending"
```

A fully commented config is generated automatically on first launch.

---

## Architecture & Performance

- **PlatformAdapter** — single interface; `PaperAdapter` and `FoliaAdapter`
  implementations are selected once at startup
- **Batching** — player updates are grouped into batches
  (`performance.batch_interval_ms`)
- **Caching** — Caffeine with individual TTLs (`placeholders.ttl`)
- **Async** — placeholder resolution off the main thread
- **Virtual threads** — Java 21 for async tasks

Check cache efficiency with `/darkrunetab stats`

---

## Roadmap (v1.0)

- [ ] Extended sorting types (name, join_time)
- [ ] Context rules for worlds and groups
- [ ] Optimizations based on pre-release testing

---

## License

This project is licensed under the **DarkRune Open License (DROL) v1.0**.

Summary:
- Free use on any servers — allowed
- Studying and modifying the code — allowed (no malicious code)
- Free distribution of unmodified copies — with Attribution
- Sale — only with Permission of the Rights Holder, or under the
  Purchaser's License after a paid purchase
- Using the source code in your own projects — only with Permission

Full text: [LICENSE](LICENSE)

---

## Support & Feedback

- Bugs and suggestions — via [Issues](../../issues)
- Include core version, plugin version, startup log and reproduction steps

---

## Other plugins by DarkRune Dev

- **DarkRune Tab** — this plugin
- Coming soon: **DarkRune Spawn**, **DarkRune Kits**

---

© 2026 DarkRune Dev
