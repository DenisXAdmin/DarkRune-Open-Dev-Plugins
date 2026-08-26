package org.darkrune.dev.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

/**
 * Встроенные плейсхолдеры плагина.
 * Разрешаются без обращения к внешним плагинам.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class InternalPlaceholders {

    private InternalPlaceholders() {
    }

    /**
     * Разрешает встроенный плейсхолдер.
     * Возвращает null, если плейсхолдер не распознан.
     *
     * @param player игрок
     * @param placeholder имя плейсхолдера (без %%)
     * @return значение или null
     */
    public static String resolve(Player player, String placeholder) {
        if (placeholder == null) {
            return null;
        }

        return switch (placeholder) {
            // Идентификаторы игрока
            case "player_name" -> player.getName();
            case "player_uuid" -> player.getUniqueId().toString();
            case "player_displayname" -> player.getName();

            // Локация
            case "player_world" -> player.getWorld().getName();
            case "player_x" -> String.valueOf(player.getLocation().getBlockX());
            case "player_y" -> String.valueOf(player.getLocation().getBlockY());
            case "player_z" -> String.valueOf(player.getLocation().getBlockZ());

            // Сеть и статус
            case "player_ping" -> String.valueOf(player.getPing());

            // Здоровье и опыт
            case "player_health" -> String.valueOf((int) player.getHealth());
            case "player_health_max" -> String.valueOf((int) player.getMaxHealth());
            case "player_level" -> String.valueOf(player.getLevel());
            case "player_exp" -> String.valueOf(player.getTotalExperience());
            case "player_food" -> String.valueOf(player.getFoodLevel());

            // Режим игры
            case "player_gamemode" -> player.getGameMode().name().toLowerCase();

            // Сервер
            case "server_online" -> String.valueOf(Bukkit.getOnlinePlayers().size());
            case "server_max_players" -> String.valueOf(Bukkit.getMaxPlayers());
            case "server_name" -> Bukkit.getName();

            // TPS (Paper API): индекс 0 = 1 мин, 1 = 5 мин, 2 = 15 мин
            case "server_tps" -> formatTps(0);
            case "server_tps_5" -> formatTps(1);
            case "server_tps_15" -> formatTps(2);

            // Время сервера
            case "server_time" -> {
                long ticks = player.getWorld().getTime();
                long hours = (ticks / 1000 + 6) % 24;
                long minutes = (ticks % 1000) * 60 / 1000;
                yield String.format("%02d:%02d", hours, minutes);
            }

            // Статистика (базовая)
            case "stat_deaths" -> String.valueOf(safeStat(player, Statistic.DEATHS));
            case "stat_jumps" -> String.valueOf(safeStat(player, Statistic.JUMP));
            case "stat_playtime_ticks" -> String.valueOf(safeStat(player, Statistic.PLAY_ONE_MINUTE));

            default -> null;
        };
    }

    /**
     * Форматирует TPS из массива Bukkit.getTPS().
     * Ограничивает значение сверху 20.00 и форматирует с двумя знаками.
     *
     * @param index индекс в массиве TPS (0 = 1 мин, 1 = 5 мин, 2 = 15 мин)
     * @return строка вида "20.00" или "19.87"
     */
    private static String formatTps(int index) {
        try {
            double[] tps = Bukkit.getTPS();
            if (tps == null || index >= tps.length) {
                return "20.00";
            }
            double value = Math.min(tps[index], 20.0D);
            return String.format("%.2f", value);
        } catch (Exception e) {
            // На некоторых платформах TPS может быть недоступен
            return "20.00";
        }
    }

    /**
     * Безопасное получение статистики игрока.
     */
    private static int safeStat(Player player, Statistic statistic) {
        try {
            return player.getStatistic(statistic);
        } catch (Exception e) {
            return 0;
        }
    }
}