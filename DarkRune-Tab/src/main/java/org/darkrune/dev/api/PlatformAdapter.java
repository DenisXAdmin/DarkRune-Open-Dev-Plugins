package org.darkrune.dev.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Интерфейс платформо-специфичного адаптера.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public interface PlatformAdapter {

    // ===== Планировщик задач =====

    void runAtPlayer(Player player, Runnable task);

    void runAsync(Runnable task);

    void runDelayed(Player player, Runnable task, long delayTicks);

    void runRepeating(Player player, Runnable task, long delayTicks, long periodTicks);

    /**
     * Периодическая глобальная задача.
     * Возвращает handle для отмены (нужно для reload).
     */
    TaskHandle runRepeatingGlobal(Runnable task, long delayTicks, long periodTicks);

    // ===== Рендеринг таб-листа =====

    void sendTabListHeaderFooter(Player player, Component header, Component footer);

    void updatePlayerDisplayName(Player viewer, Player target, Component displayName);

    // ===== Scoreboard =====

    Scoreboard getSharedScoreboard();

    // ===== Батчинг обновлений =====

    void queueUpdate(Player player);

    // ===== Жизненный цикл =====

    void cancelAllTasks();

    String getPlatformName();
}