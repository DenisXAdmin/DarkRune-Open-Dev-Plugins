# DarkRune Tab

[![Pre-release](https://img.shields.io/badge/status-pre--release-orange)]()
[![License](https://img.shields.io/badge/license-DROL%20v1.0-blue)]()
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-yellow)]()
[![Folia](https://img.shields.io/badge/Folia-supported-green)]()
[![Java](https://img.shields.io/badge/Java-21-red)]()

Высокопроизводительный плагин **таб-листа**, **неймтегов**, **скорборда**
и **форматирования чата** для Paper 1.21+ с нативной поддержкой Folia.

> English version: [README.md](README.md)

---

## Особенности

- **Tab List** — хедер/футер с MiniMessage (градиенты, hover, click)
- **Nametags** — префиксы/суффиксы над головой через scoreboard teams
- **Scoreboard** — боковая панель с анти-мерцанием
- **Форматирование чата** — форматы по правам с приоритетами
- **Полный контроль строки игрока в табе** (ники, HP, титулы)
- **Нативные адаптеры Paper и Folia**
- **Глубокая интеграция с LuckPerms** — префиксы, суффиксы, сортировка
  по весу, мгновенное обновление при смене группы
- **PlaceholderAPI** + встроенные плейсхолдеры (включая TPS)
- **Умное кэширование** — индивидуальный TTL для каждого плейсхолдера
- **Производительность** — асинхронность, батчинг, виртуальные потоки

---

## Требования

| Компонент | Версия | Обязательно |
|---|---|---|
| Java | 21+ | Да |
| Paper / Folia | 1.21+ | Да |
| LuckPerms | 5.x | Нет |
| PlaceholderAPI | 2.x | Нет |

---

## Установка

1. Скачайте `DarkRuneTab-<version>.jar` из [Releases](../../releases)
2. Поместите в `plugins/`
3. Перезапустите сервер
4. Настройте `plugins/DarkRuneTab/config.yml`
5. Выполните `/darkrunetab reload`

---

## Сборка

```bash
cd DarkRune-Tab
mvn clean package
```

---

## Команды и права

| Команда | Описание | Право |
|---|---|---|
| `/darkrunetab help` | Справка | `darkrunetab.use` |
| `/darkrunetab reload` | Перезагрузка (мгновенно) | `darkrunetab.reload` |
| `/darkrunetab toggle <module>` | Вкл/выкл модуль | `darkrunetab.toggle` |
| `/darkrunetab debug` | Отладка | `darkrunetab.debug` |
| `/darkrunetab stats` | Статистика кэша | `darkrunetab.debug` |

Алиасы: `/dtab`, `/tab`

---

## Модули

| Модуль | Описание | По умолчанию |
|---|---|---|
| `tablist` | Хедер, футер, формат игрока | Включён |
| `nametags` | Префикс/суффикс над головой | Включён |
| `scoreboard` | Боковая панель | Выключен |
| `chat` | Форматирование чата | Включён |

---

## Почему DarkRune Tab

- **Ваш LuckPerms работает сразу** — веса и префиксы читаются из коробки.
- **Имя в табе — это формат**, а не константа.
- **Проще конфиг — больше контроля.**
- **Лёгкий и современный** (Java 21).
- **Дружелюбная к реселлерам лицензия (DROL).**
- **Производительность на уровне индустриального стандарта.**

---

## Лицензия

**DarkRune Open License (DROL) v1.0.**
См. [LICENSE](LICENSE) (копия корневого [LICENSE](../../LICENSE)).

---

## Поддержка

Баги и предложения — через [Issues](../../issues).

---

© 2026 DarkRune Dev