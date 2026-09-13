# DarkRune Tab

[![Release](https://img.shields.io/badge/status-stable-green)]()
[![License](https://img.shields.io/badge/license-DROL%20v1.0-blue)]()
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-yellow)]()
[![Folia](https://img.shields.io/badge/Folia-supported-green)]()
[![Java](https://img.shields.io/badge/Java-21-red)]()

Высокопроизводительный плагин **таб-листа**, **неймтегов**, **скорборда**
и **форматирования чата** для Paper 1.21+ с нативной поддержкой Folia.

> English version: [README.md](README.md)

> 🎯 **TL;DR:** Ваш LuckPerms работает сразу. Без параллельных систем,
> без часов настройки — правильный таб и неймтеги за минуты.

---

## Особенности

- **Tab List** — хедер/футер с MiniMessage (градиенты, hover, click)
- **Nametags** — префиксы/суффиксы над головой через scoreboard teams
- **Scoreboard** — боковая панель с анти-мерцанием (обновляются только
  изменившиеся строки)
- **Форматирование чата** — форматы по правам с приоритетами, контроль
  цветов игроков (отключено по умолчанию)
- **Полный контроль строки игрока в табе** — имя собирается из
  плейсхолдеров: ники, HP, титулы, скрытые имена
- **Нативные адаптеры Paper и Folia** — выбираются один раз при старте,
  никаких условных переходов в горячих путях
- **Глубокая интеграция с LuckPerms** — префиксы, суффиксы, сортировка по
  весу группы, мгновенные обновления при смене группы
- **PlaceholderAPI** + встроенные плейсхолдеры (включая TPS)
- **Умное кэширование** — индивидуальный TTL для каждого плейсхолдера
- **Производительность** — асинхронность, батчинг, виртуальные потоки
  (Java 21)

---

## Требования

| Компонент | Версия | Обязательно |
|---|---|---|
| Java | 21+ | Да |
| Paper / Folia | 1.21+ | Да |
| LuckPerms | 5.x | Нет (префиксы/сортировка) |
| PlaceholderAPI | 2.x | Нет (внешние плейсхолдеры) |

---

## Установка

1. Скачайте `DarkRuneTab-<version>.jar` из [Releases](../../releases)
2. Поместите файл в папку `plugins/`
3. Перезапустите сервер
4. Настройте `plugins/DarkRuneTab/config.yml`
5. Выполните `/darkrunetab reload`

---

## Сборка из исходников

```bash
cd DarkRune-Tab
mvn clean package
```

Готовый JAR появится в `target/DarkRuneTab-<version>.jar`.

---

## Команды и права

| Команда | Описание | Право |
|---|---|---|
| `/darkrunetab help` | Справка | `darkrunetab.use` |
| `/darkrunetab reload` | Перезагрузка конфигов (мгновенно) | `darkrunetab.reload` |
| `/darkrunetab toggle <module>` | Вкл/выкл модуль | `darkrunetab.toggle` |
| `/darkrunetab debug` | Отладочная информация | `darkrunetab.debug` |
| `/darkrunetab stats` | Статистика кэша | `darkrunetab.debug` |

Алиасы: `/dtab`, `/tab`

---

## Модули

| Модуль | Описание | По умолчанию |
|---|---|---|
| `tablist` | Хедер, футер, формат игрока | Включён |
| `nametags` | Префикс/суффикс над головой | Включён |
| `scoreboard` | Боковая панель с анти-мерцанием | Выключен |
| `chat` | Форматирование чата с приоритетами | **Выключен** (включается вручную) |

---

## Встроенные плейсхолдеры

| Плейсхолдер | Описание |
|---|---|
| `%player_name%` / `%player_uuid%` / `%player_displayname%` | Идентификация |
| `%player_world%` / `%player_x%` / `%player_y%` / `%player_z%` | Локация |
| `%player_ping%` / `%player_health%` / `%player_food%` | Статус |
| `%player_level%` / `%player_exp%` / `%player_gamemode%` | Прогресс |
| `%server_online%` / `%server_max_players%` / `%server_name%` | Сервер |
| `%server_tps%` / `%server_tps_5%` / `%server_tps_15%` | TPS |
| `%server_time%` | Игровое время (24-часовой формат) |
| `%stat_deaths%` / `%stat_jumps%` | Статистика |

### Плейсхолдеры LuckPerms

| Плейсхолдер | Описание |
|---|---|
| `%display_lp_prefix%` / `%display_lp_suffix%` | Префикс / суффикс |
| `%display_lp_primary_group%` | Основная группа |
| `%display_lp_primary_group_displayname%` | Отображаемое имя группы |
| `%display_lp_group_weight%` / `%display_lp_group_color%` | Вес / цвет |
| `%display_lp_meta_<key>%` | Кастомное мета-значение |
| `%display_lp_has_group_<name>%` | true/false |

---

## Пример конфигурации

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
      # Полный контроль строки игрока в табе
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
    enabled: false  # Отключён по умолчанию
    default_format: "%display_lp_prefix%%player_name%&7: &f%message%"
    formats:
      admin:
        permission: "darkrunetab.chat.format.admin"
        priority: 100
        format: "&c[ADMIN] %player_name%&c: &f%message%"
```

Полностью прокомментированный конфиг генерируется при первом запуске.

---

## Почему DarkRune Tab

- **Ваш LuckPerms работает сразу.** Веса, префиксы и суффиксы читаются из
  LuckPerms из коробки — правильный таб за минуты, а не часы.
- **Имя в табе — это формат, а не константа.** Собирайте всю строку таба
  из плейсхолдеров: ники, HP, титулы, скрытые имена.
- **Проще конфиг — больше контроля.** Небольшая сфокусированная
  конфигурация с полной гибкостью там, где это важно.
- **Лёгкий и современный.** Java 21, чистая модульная архитектура, легко
  читать и форкать.
- **Лицензия, дружелюбная к реселлерам (DROL).** Перепродажа и партнёрка
  разрешены, в отличие от жёстких лицензий конкурентов.
- **Производительность на уровне индустриального стандарта** при
  эквивалентной нагрузке.

---

## Архитектура и производительность

- Платформенные адаптеры выбираются один раз при старте
- Батчинг обновлений игроков
- Кэши Caffeine с индивидуальными TTL для каждого плейсхолдера
- Асинхронное разрешение плейсхолдеров, виртуальные потоки

**Бенчмарк:** стабильно 80+ одновременных подключений при ~20 TPS
(Ryzen 5 5600G, 6 GB heap). Вклад плагина — **<1%** от тик-бюджета.

---

## Roadmap (v1.1+)

- [ ] Расширенные типы сортировки (name, join_time)
- [ ] Контекстные правила для миров и групп
- [ ] Конфигурации для каждого мира
- [ ] Дополнительные встроенные плейсхолдеры

---

## Лицензия

Плагин распространяется под **DarkRune Open License (DROL) v1.0**.

Кратко:
- Бесплатное использование на любых серверах — разрешено
- Изучение и модификация кода — разрешены (без вредоносного кода)
- Бесплатное распространение неизменённых копий — с атрибуцией
- Продажа — только с разрешения правообладателя или по Лицензии Покупателя
- Использование исходного кода в своих проектах — только с разрешения

Полный текст: [LICENSE](LICENSE) (копия корневого [LICENSE](../../LICENSE)).

---

## Поддержка

Баги и предложения — через [Issues](../../issues).

Пожалуйста, приложите:
- Ядро и версию сервера (Paper / Folia)
- Версию плагина
- Лог запуска
- Шаги воспроизведения

---

## Другие проекты DarkRune Dev

- **DarkRune Tab** — этот плагин
- Скоро: **DarkRune Spawn**, **DarkRune Kits**

---

© 2026 DarkRune Dev
