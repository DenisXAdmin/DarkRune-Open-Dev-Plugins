package org.darkrune.dev.integration.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.cache.Caches;

import java.util.UUID;

/**
 * Интеграция с LuckPerms.
 * Получает префиксы, суффиксы, группы и веса через официальный API.
 * Подписывается на события для автоматической инвалидации кэша.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class LuckPermsIntegration {

    private final DarkRuneTab plugin;
    private final LuckPerms luckPerms;
    private final boolean available;

    public LuckPermsIntegration(DarkRuneTab plugin) {
        this.plugin = plugin;

        LuckPerms api = null;
        boolean apiAvailable = false;
        try {
            api = LuckPermsProvider.get();
            apiAvailable = true;
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("LuckPerms API not available: " + e.getMessage());
        }
        this.luckPerms = api;
        this.available = apiAvailable;

        if (available) {
            registerEventListeners();
        }
    }

    /**
     * Подписка на события LuckPerms для инвалидации кэша при изменениях.
     */
    private void registerEventListeners() {
        luckPerms.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();

            // Инвалидируем кэш мета-данных
            Caches.clearPlayer(uuid);

            // Запланировать обновление отображения игрока
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getAdapter().queueUpdate(player);
            }
        });
    }

    /**
     * Получить мета-данные игрока (используется кэш).
     */
    public MetaData getMetaData(UUID uuid) {
        if (!available || uuid == null) {
            return MetaData.empty();
        }

        Object cached = Caches.METADATA_CACHE.getIfPresent(uuid);
        if (cached instanceof MetaData data) {
            return data;
        }

        MetaData loaded = loadMetaData(uuid);
        Caches.METADATA_CACHE.put(uuid, loaded);
        return loaded;
    }

    /**
     * Получить префикс игрока.
     */
    public String getPrefix(UUID uuid) {
        return getMetaData(uuid).prefix();
    }

    /**
     * Получить суффикс игрока.
     */
    public String getSuffix(UUID uuid) {
        return getMetaData(uuid).suffix();
    }

    /**
     * Получить вес основной группы.
     */
    public int getGroupWeight(UUID uuid) {
        return getMetaData(uuid).groupWeight();
    }

    /**
     * Получить основную группу (техническое имя).
     */
    public String getPrimaryGroup(UUID uuid) {
        return getMetaData(uuid).primaryGroup();
    }

    /**
     * Получить отображаемое имя основной группы.
     */
    public String getPrimaryGroupDisplayName(UUID uuid) {
        return getMetaData(uuid).primaryGroupDisplayName();
    }

    /**
     * Получить цвет группы из LuckPerms.
     */
    public String getGroupColor(UUID uuid) {
        return getMetaData(uuid).groupColor();
    }

    /**
     * Проверить, имеет ли игрок конкретное право.
     */
    public boolean hasPermission(UUID uuid, String permission) {
        if (!available || uuid == null || permission == null) {
            return false;
        }
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return false;
        }
        return user.getCachedData().getPermissionData()
                .checkPermission(permission).asBoolean();
    }

    /**
     * Получить значение кастомной мета-переменной.
     */
    public String getMetaValue(UUID uuid, String key) {
        if (!available || uuid == null || key == null) {
            return null;
        }
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return null;
        }
        return user.getCachedData().getMetaData().getMetaValue(key);
    }

    /**
     * Принудительно обновить кэш для игрока.
     */
    public void invalidateCache(UUID uuid) {
        Caches.clearPlayer(uuid);
    }

    /**
     * Очистить весь кэш мета-данных.
     */
    public void clearCache() {
        Caches.METADATA_CACHE.invalidateAll();
    }

    /**
     * Получить статистику кэша.
     */
    public String getCacheStats() {
        return String.format("MetaData cache: %d entries (hit rate: %.1f%%)",
                Caches.METADATA_CACHE.estimatedSize(),
                Caches.METADATA_CACHE.stats().hitRate() * 100);
    }

    // ===== Внутренние методы =====

    /**
     * Загружает мета-данные из LuckPerms (без кэширования).
     */
    private MetaData loadMetaData(UUID uuid) {
        if (!available) {
            return MetaData.empty();
        }

        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return MetaData.empty();
        }

        CachedMetaData cached = user.getCachedData().getMetaData();

        String prefix = cached.getPrefix() != null ? cached.getPrefix() : "";
        String suffix = cached.getSuffix() != null ? cached.getSuffix() : "";
        String primaryGroup = user.getPrimaryGroup() != null ? user.getPrimaryGroup() : "default";

        String displayName = primaryGroup;
        int weight = 0;
        String color = "WHITE";

        Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
        if (group != null) {
            displayName = group.getDisplayName() != null ? group.getDisplayName() : primaryGroup;
            weight = group.getWeight().orElse(0);
            color = extractGroupColor(group);
        }

        return new MetaData(prefix, suffix, primaryGroup, displayName, color, weight);
    }

    /**
     * Извлекает цвет группы из meta-узлов.
     * LuckPerms может хранить цвет в мета-ключе "color" или "namecolor".
     */
    private String extractGroupColor(Group group) {
        for (MetaNode node : group.getNodes(NodeType.META)) {
            String key = node.getMetaKey();
            if ("color".equalsIgnoreCase(key) || "namecolor".equalsIgnoreCase(key)) {
                String value = node.getMetaValue();
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return "WHITE";
    }
}