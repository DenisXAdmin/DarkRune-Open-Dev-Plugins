package org.darkrune.dev.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Интерфейс платформо-специфичного адаптера.
 * Реализации для разных ядер (PaperAdapter, FoliaAdapter)
 * предоставляют оптимизированные методы под свою платформу.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public interface PlatformAdapter {

    // ===== Планировщик задач =====

    /**
     * Выполнить задачу в контексте игрока.
     * - Paper: основной поток сервера
     * - Folia: регион, в котором находится игрок
     */
    void runAtPlayer(Player player, Runnable task);

    /**
     * Выполнить задачу асинхронно.
     */
    void runAsync(Runnable task);

    /**
     * Выполнить задачу с задержкой.
     */
    void runDelayed(Player player, Runnable task, long delayTicks);

    /**
     * Выполнять задачу периодически (привязана к игроку).
     */
    void runRepeating(Player player, Runnable task, long delayTicks, long periodTicks);

    /**
     * Выполнять задачу периодически глобально (не привязана к игроку).
     * - Paper: основной поток
     * - Folia: глобальный регион
     */
    void runRepeatingGlobal(Runnable task, long delayTicks, long periodTicks);

    // ===== Рендеринг таб-листа =====

    /**
     * Отправить хедер и футер таб-листа игроку.
     */
    void sendTabListHeaderFooter(Player player, Component header, Component footer);

    /**
     * Обновить отображаемое имя игрока в таб-листе у зрителя.
     */
    void updatePlayerDisplayName(Player viewer, Player target, Component displayName);

    // ===== Scoreboard =====

    /**
     * Получить общий scoreboard (используется для teams и objective).
     * Один scoreboard на всех игроков минимизирует сетевой трафик.
     */
    Scoreboard getSharedScoreboard();

    // ===== Батчинг обновлений =====

    /**
     * Добавить игрока в очередь обновлений.
     */
    void queueUpdate(Player player);

    // ===== Управление жизненным циклом =====

    /**
     * Отменить все активные задачи адаптера.
     */
    void cancelAllTasks();

    /**
     * Получить название платформы (для логов и отладки).
     */
    String getPlatformName();
}