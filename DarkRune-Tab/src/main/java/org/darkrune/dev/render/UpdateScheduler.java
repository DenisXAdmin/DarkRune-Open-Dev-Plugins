package org.darkrune.dev.render;

import org.bukkit.entity.Player;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.module.DisplayModule;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Планировщик асинхронных обновлений отображения.
 * Вызывает update() на всех активных модулях для игрока.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class UpdateScheduler {

    private final DarkRuneTab plugin;
    private final ConcurrentHashMap<UUID, Boolean> activePlayers = new ConcurrentHashMap<>();

    public UpdateScheduler(DarkRuneTab plugin) {
        this.plugin = plugin;
    }

    /**
     * Обработать обновление для игрока.
     * Вызывает update() на всех активных модулях.
     */
    public void processUpdate(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        activePlayers.put(uuid, Boolean.TRUE);

        var moduleManager = plugin.getModuleManager();
        if (moduleManager == null) {
            return;
        }

        for (DisplayModule module : moduleManager.getActiveModules()) {
            try {
                module.update(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Error updating module "
                        + module.getName() + " for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Отменить активные задачи для конкретного игрока.
     */
    public void cancelForPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }

    /**
     * Отменить все задачи при выключении плагина.
     */
    public void shutdown() {
        activePlayers.clear();
    }

    /**
     * Проверить, активен ли игрок в системе обновлений.
     */
    public boolean isPlayerActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    /**
     * Получить количество активных игроков.
     */
    public int getActivePlayerCount() {
        return activePlayers.size();
    }
}