# DarkRune Tab

[![Pre-release](https://img.shields.io/badge/status-pre--release-orange)]()
[![License](https://img.shields.io/badge/license-DROL%20v1.0-blue)]()
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-yellow)]()
[![Folia](https://img.shields.io/badge/Folia-supported-green)]()
[![Java](https://img.shields.io/badge/Java-21-red)]()

Высокопроизводительный плагин для управления **таб-листом**, **неймтегами**
и **скорбордом** на серверах Paper 1.21+ с нативной поддержкой Folia.

> English version: [README.md](README.md)

---

## Особенности

- **Tab List** — настраиваемые хедер и футер с поддержкой MiniMessage
  (градиенты, hover, click-события)
- **Nametags** — префиксы и суффиксы над головой и в таб-листе через
  scoreboard teams
- **Scoreboard** — боковая панель с анти-мерцанием (обновляются только
  изменившиеся строки)
- **Нативная поддержка Paper и Folia** — платформенные адаптеры без
  условных ветвлений в горячих путях
- **Глубокая интеграция с LuckPerms** — префиксы, суффиксы, веса групп,
  сортировка игроков по весу группы
- **PlaceholderAPI** + собственные встроенные плейсхолдеры
- **Умное кэширование** — индивидуальные TTL для каждого плейсхолдера,
  кэш мета-данных LuckPerms
- **Производительность** — асинхронное разрешение плейсхолдеров, батчинг
  обновлений, виртуальные потоки (Java 21)

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
git clone <url-репозитория>
cd DarkRune-Tab
mvn clean package
```

Готовый JAR появится в `target/DarkRuneTab-<version>.jar`.

---

## Команды и права

| Команда | Описание | Право |
|---|---|---|
| `/darkrunetab help` | Справка | `darkrunetab.use` |
| `/darkrunetab reload` | Перезагрузка конфигов | `darkrunetab.reload` |
| `/darkrunetab toggle <module>` | Вкл/выкл модуль | `darkrunetab.toggle` |
| `/darkrunetab debug` | Отладочная информация | `darkrunetab.debug` |
| `/darkrunetab stats` | Статистика кэша | `darkrunetab.debug` |

Алиасы: `/dtab`, `/tab`

---

## Модули

| Модуль | Описание | По умолчанию |
|---|---|---|
| `tablist` | Хедер и футер таб-листа | Включён |
| `nametags` | Префиксы/суффиксы над головой | Включён |
| `scoreboard` | Боковая панель | Выключен |

Переключение: `/darkrunetab toggle <module>` или в `config.yml`.

---

## Встроенные плейсхолдеры

| Плейсхолдер | Описание |
|---|---|
| `%player_name%` | Ник игрока |
| `%player_uuid%` | UUID |
| `%player_displayname%` | Отображаемое имя |
| `%player_world%` | Текущий мир |
| `%player_x% / %player_y% / %player_z%` | Координаты |
| `%player_ping%` | Пинг (мс) |
| `%player_health%` / `%player_health_max%` | Здоровье |
| `%player_level%` / `%player_exp%` | Уровень / опыт |
| `%player_food%` | Голод |
| `%player_gamemode%` | Режим игры |
| `%server_online%` | Онлайн |
| `%server_max_players%` | Слоты |
| `%server_name%` | Имя сервера |
| `%server_tps%` | TPS (1 мин) |
| `%server_tps_5%` / `%server_tps_15%` | TPS (5 / 15 мин) |
| `%server_time%` | Игровое время |
| `%stat_deaths%` / `%stat_jumps%` | Статистика |

### Плейсхолдеры LuckPerms

| Плейсхолдер | Описание |
|---|---|
| `%display_lp_prefix%` | Префикс |
| `%display_lp_suffix%` | Суффикс |
| `%display_lp_primary_group%` | Основная группа |
| `%display_lp_primary_group_displayname%` | Отображаемое имя группы |
| `%display_lp_group_weight%` | Вес группы |
| `%display_lp_group_color%` | Цвет группы (meta `color`) |
| `%display_lp_meta_<key>%` | Кастомная meta-переменная |
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
        <gray>TPS: <green>%server_tps%</green> | Ping: <yellow>%player_ping%ms</yellow>
      update_interval: 5
    player_format:
      format: "%display_lp_prefix%%player_name%%display_lp_suffix%"
    sorting:
      enabled: true
      type: "luckperms_weight"
      direction: "descending"
```

Полный конфиг с комментариями создаётся автоматически при первом запуске.

---

## Архитектура и производительность

- **PlatformAdapter** — единый интерфейс; отдельные реализации
  `PaperAdapter` и `FoliaAdapter` выбираются один раз при старте
- **Батчинг** — обновления игроков объединяются в пакеты
  (`performance.batch_interval_ms`)
- **Кэши** — Caffeine с индивидуальными TTL (`placeholders.ttl`)
- **Асинхронность** — разрешение плейсхолдеров вне главного потока
- **Виртуальные потоки** — Java 21 для асинхронных задач

Проверить эффективность кэша: `/darkrunetab stats`

---

## Roadmap (планы на v1.0)

- [ ] Расширенные типы сортировки (name, join_time)
- [ ] Контекстные правила для миров и групп
- [ ] Оптимизации по итогам тестирования пре-релиза

---

## Лицензия

Проект распространяется под **DarkRune Open License (DROL) v1.0**.

Кратко:
- Бесплатное использование на любых серверах — разрешено
- Изучение и модификация кода — разрешены (без вредоносного кода)
- Бесплатное распространение неизменённых копий — с атрибуцией
- Продажа — только с разрешения правообладателя или по Лицензии
  Покупателя после платной покупки
- Использование исходного кода в своих проектах — только с разрешения

Полный текст: [LICENSE](LICENSE)

---

## Поддержка и обратная связь

- Баги и предложения — через [Issues](../../issues)
- Приложите версию ядра, версию плагина, лог запуска и шаги воспроизведения

---

## Другие плагины DarkRune Dev

- **DarkRune Tab** — этот плагин
- Скоро: **DarkRune Spawn**, **DarkRune Kits**

---

© 2026 DarkRune Dev
