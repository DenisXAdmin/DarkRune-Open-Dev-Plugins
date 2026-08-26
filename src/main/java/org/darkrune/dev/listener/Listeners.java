package org.darkrune.dev.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.cache.Caches;

/**
 * Все слушатели событий плагина.
 * Вложенные классы для каждого типа событий.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public final class Listeners {

    private Listeners() {
    }

    /**
     * Регистрация всех слушателей.
     *
     * @param plugin экземпляр плагина
     */
    public static void registerAll(DarkRuneTab plugin) {
        var pm = plugin.getServer().getPluginManager();

        pm.registerEvents(new PlayerListener(plugin), plugin);
        pm.registerEvents(new WorldListener(plugin), plugin);

        plugin.getLogger().info("Listeners registered");
    }

    // ===== Вложенные слушатели =====

    /**
     * Слушатель событий игрока (вход, выход).
     */
    public static class PlayerListener implements Listener {

        private final DarkRuneTab plugin;

        public PlayerListener(DarkRuneTab plugin) {
            this.plugin = plugin;
        }

        /**
         * Игрок зашёл на сервер.
         * Инициализируем таб-лист и загружаем мета-данные.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();

            // Загружаем мета-данные из LuckPerms (в кэш)
            plugin.getLuckPerms().getMetaData(player.getUniqueId());

            // Планируем обновление отображения
            plugin.getAdapter().queueUpdate(player);
        }

        /**
         * Игрок вышел с сервера.
         * Очищаем кэш и отменяем задачи.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();

            // Очищаем кэш игрока
            Caches.clearPlayer(player.getUniqueId());

            // Отменяем активные задачи для игрока
            plugin.getUpdateScheduler().cancelForPlayer(player.getUniqueId());
        }
    }

    /**
     * Слушатель событий мира.
     */
    public static class WorldListener implements Listener {

        private final DarkRuneTab plugin;

        public WorldListener(DarkRuneTab plugin) {
            this.plugin = plugin;
        }

        /**
         * Игрок сменил мир.
         * Планируем обновление таб-листа (могут быть контекстные правила).
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onWorldChange(PlayerChangedWorldEvent event) {
            Player player = event.getPlayer();

            // Инвалидируем кэш плейсхолдеров для этого игрока
            plugin.getPlaceholderResolver().invalidatePlayerCache(player);

            // Планируем обновление
            plugin.getAdapter().queueUpdate(player);
        }
    }
}