# DarkRune Tab

[![Release](https://img.shields.io/badge/status-stable-green)]()
[![License](https://img.shields.io/badge/license-DROL%20v1.0-blue)]()
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-yellow)]()
[![Folia](https://img.shields.io/badge/Folia-supported-green)]()
[![Java](https://img.shields.io/badge/Java-21-red)]()

High-performance **Tab List**, **Nametags**, **Scoreboard** and **Chat
formatting** plugin for Paper 1.21+ with native Folia support.

> Русская версия: [README-RU.md](README-RU.md)

> 🎯 **TL;DR:** Your LuckPerms setup just works. No parallel systems,
> no hours of configuration — correct tab order and nametags in minutes.

---

## Features

- **Tab List** — customizable header/footer with MiniMessage
  (gradients, hover, click)
- **Nametags** — prefixes/suffixes above the head via scoreboard teams
- **Scoreboard** — side panel with anti-flicker (only changed lines update)
- **Chat formatting** — permission-based formats with priorities and
  player color control (disabled by default)
- **Full placeholder-driven player name** in the tab list — nicknames,
  HP, titles, hidden names
- **Native Paper & Folia adapters** — selected once at startup, no
  conditional branching in hot paths
- **Deep LuckPerms integration** — prefixes, suffixes, group weight
  sorting, instant updates on group change
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
2. Place it into the `plugins/` folder
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
| `scoreboard` | Side panel with anti-flicker | Disabled |
| `chat` | Chat formatting with priorities | **Disabled** (enable manually) |

---

## Built-in Placeholders

| Placeholder | Description |
|---|---|
| `%player_name%` / `%player_uuid%` / `%player_displayname%` | Identity |
| `%player_world%` / `%player_x%` / `%player_y%` / `%player_z%` | Location |
| `%player_ping%` / `%player_health%` / `%player_food%` | Status |
| `%player_level%` / `%player_exp%` / `%player_gamemode%` | Progress |
| `%server_online%` / `%server_max_players%` / `%server_name%` | Server |
| `%server_tps%` / `%server_tps_5%` / `%server_tps_15%` | TPS |
| `%server_time%` | In-game time (24-hour format) |
| `%stat_deaths%` / `%stat_jumps%` | Statistics |

### LuckPerms Placeholders

| Placeholder | Description |
|---|---|
| `%display_lp_prefix%` / `%display_lp_suffix%` | Prefix / suffix |
| `%display_lp_primary_group%` | Primary group |
| `%display_lp_primary_group_displayname%` | Group display name |
| `%display_lp_group_weight%` / `%display_lp_group_color%` | Weight / color |
| `%display_lp_meta_<key>%` | Custom meta value |
| `%display_lp_has_group_<name>%` | true/false |

---

## Configuration Example

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
        <gray>TPS: <green>%server_tps%</green>
      update_interval: 5
    player_format:
      # Full control over the tab entry string
      format: "%display_lp_prefix%%player_name%%display_lp_suffix%"
      update_interval: 0
    sorting:
      enabled: true
      type: "luckperms_weight"
      direction: "descending"

  nametags:
    enabled: true
    format:
      prefix: "%display_lp_prefix%"
      name: "%player_name%"
      suffix: "%display_lp_suffix%"

  chat:
    enabled: false  # Disabled by default
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
  read from LuckPerms out of the box — correct tab in minutes, not hours.
- **The displayed name is a format, not a constant.** Compose the whole tab
  string from placeholders: nicknames, HP, titles, hidden names.
- **Simpler config, more control.** A small, focused configuration with
  full flexibility where it matters.
- **Lightweight and modern.** Java 21, clean modular architecture, easy to
  read and fork.
- **Reseller-friendly license (DROL).** Redistribution and a partner
  program are allowed, unlike restrictive competitor licenses.
- **Performance on par with the industry standard** at equivalent load.

---

## Architecture & Performance

- Platform adapters selected once at startup
- Batching of player updates
- Caffeine caches with per-placeholder TTL
- Async placeholder resolution, virtual threads

**Benchmark:** stable at 80+ concurrent connections at ~20 TPS
(Ryzen 5 5600G, 6 GB heap). Plugin overhead — **<1%** of tick budget.

---

## Roadmap (v1.1+)

- [ ] Extended sorting types (name, join_time)
- [ ] Context rules for worlds and groups
- [ ] Per-world configurations
- [ ] Additional built-in placeholders

---

## License

This plugin is licensed under the **DarkRune Open License (DROL) v1.0**.

Summary:
- Free use on any servers — allowed
- Studying and modifying the code — allowed (no malicious code)
- Free distribution of unmodified copies — with attribution
- Sale — only with the rights holder's permission or under the
  Purchaser's License
- Using the source code in your own projects — only with permission

Full text: [LICENSE](LICENSE) (copy of the root [LICENSE](../../LICENSE)).

---

## Support

Bugs and suggestions — via [Issues](../../issues).

Please include:
- Server core and version (Paper / Folia)
- Plugin version
- Startup log
- Steps to reproduce

---

## Other Projects by DarkRune Dev

- **DarkRune Tab** — this plugin
- Coming soon: **DarkRune Spawn**, **DarkRune Kits**

---

© 2026 DarkRune Dev
