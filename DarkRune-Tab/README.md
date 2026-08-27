# DarkRune Tab

[![Pre-release](https://img.shields.io/badge/status-pre--release-orange)]()
[![License](https://img.shields.io/badge/license-DROL%20v1.0-blue)]()
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-yellow)]()
[![Folia](https://img.shields.io/badge/Folia-supported-green)]()
[![Java](https://img.shields.io/badge/Java-21-red)]()

High-performance **Tab List**, **Nametags**, **Scoreboard** and **Chat
formatting** plugin for Paper 1.21+ with native Folia support.

> Русская версия: [README-RU.md](README-RU.md)

---

## Features

- **Tab List** — customizable header/footer with MiniMessage
  (gradients, hover, click)
- **Nametags** — prefixes/suffixes above the head via scoreboard teams
- **Scoreboard** — side panel with anti-flicker (only changed lines update)
- **Chat formatting** — permission-based formats with priorities and
  player color control
- **Full placeholder-driven player name** in the tab list
  (nicknames, HP, titles, hidden names)
- **Native Paper & Folia** — platform adapters, no branching in hot paths
- **Deep LuckPerms integration** — prefixes, suffixes, group weight sorting,
  instant updates on group change
- **PlaceholderAPI** + built-in placeholders (incl. TPS)
- **Smart caching** — individual TTL per placeholder
- **Performance** — async resolution, batching, virtual threads (Java 21)

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
2. Place it into `plugins/`
3. Restart the server
4. Adjust `plugins/DarkRuneTab/config.yml`
5. Run `/darkrunetab reload`

---

## Building from source

```bash
cd DarkRune-Tab
mvn clean package
```

The JAR appears at `target/DarkRuneTab-<version>.jar`.

---

## Commands & Permissions

| Command | Description | Permission |
|---|---|---|
| `/darkrunetab help` | Show help | `darkrunetab.use` |
| `/darkrunetab reload` | Reload configs (instant apply) | `darkrunetab.reload` |
| `/darkrunetab toggle <module>` | Enable/disable a module | `darkrunetab.toggle` |
| `/darkrunetab debug` | Debug info | `darkrunetab.debug` |
| `/darkrunetab stats` | Cache statistics | `darkrunetab.debug` |

Aliases: `/dtab`, `/tab`

---

## Modules

| Module | Description | Default |
|---|---|---|
| `tablist` | Header, footer, player name format | Enabled |
| `nametags` | Prefix/suffix above the head | Enabled |
| `scoreboard` | Side panel | Disabled |
| `chat` | Chat formatting | Enabled |

---

## Built-in placeholders

| Placeholder | Description |
|---|---|
| `%player_name%` / `%player_uuid%` / `%player_displayname%` | Identity |
| `%player_world%` / `%player_x%` / `%player_y%` / `%player_z%` | Location |
| `%player_ping%` / `%player_health%` / `%player_food%` | Status |
| `%player_level%` / `%player_exp%` / `%player_gamemode%` | Progress |
| `%server_online%` / `%server_max_players%` / `%server_name%` | Server |
| `%server_tps%` / `%server_tps_5%` / `%server_tps_15%` | TPS |
| `%server_time%` | In-game time |
| `%stat_deaths%` / `%stat_jumps%` | Statistics |

### LuckPerms placeholders

| Placeholder | Description |
|---|---|
| `%display_lp_prefix%` / `%display_lp_suffix%` | Prefix / suffix |
| `%display_lp_primary_group%` | Primary group |
| `%display_lp_primary_group_displayname%` | Group display name |
| `%display_lp_group_weight%` / `%display_lp_group_color%` | Weight / color |
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
        <gray>Online: <aqua>%server_online%</aqua>
      update_interval: 5
    player_format:
      # Full control over the tab entry string
      format: "%display_lp_prefix%%player_name%%display_lp_suffix%"
      update_interval: 0
    sorting:
      enabled: true
      type: "luckperms_weight"
      direction: "descending"

  chat:
    enabled: true
    default_format: "%display_lp_prefix%%player_name%&7: &f%message%"
    formats:
      admin:
        permission: "darkrunetab.chat.format.admin"
        priority: 100
        format: "&c[ADMIN] %player_name%&c: &f%message%"
```

A fully commented config is generated on first launch.

---

## Why DarkRune Tab

- **Your LuckPerms setup just works.** Weights, prefixes and suffixes are
  read from LuckPerms out of the box — correct tab order in minutes.
- **The displayed name is a format, not a constant.** Compose the whole tab
  string from placeholders.
- **Simpler config, more control.**
- **Lightweight and modern.** Java 21, clean modular architecture.
- **Reseller-friendly license (DROL).**
- **Performance on par with the industry standard** at equivalent load.

---

## Architecture & Performance

- Platform adapters selected once at startup
- Batching of player updates
- Caffeine caches with per-placeholder TTL
- Async placeholder resolution, virtual threads

Benchmark: stable at 80+ concurrent connections at ~20 TPS
(Ryzen 5 5600G, 6 GB heap). Overhead <1% of tick budget.

---

## Roadmap (v1.0)

- [ ] Extended sorting types (name, join_time)
- [ ] Context rules for worlds and groups
- [ ] Optimizations from pre-release testing

---

## License

Licensed under the **DarkRune Open License (DROL) v1.0**.
See [LICENSE](LICENSE) (copy of the root [LICENSE](../../LICENSE)).

---

## Support

Bugs and suggestions — via [Issues](../../issues).

---

© 2026 DarkRune Dev