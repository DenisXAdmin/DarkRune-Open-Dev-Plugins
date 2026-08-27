package org.darkrune.dev.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.cache.Caches;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.integration.luckperms.LuckPermsPlaceholders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разрешение плейсхолдеров в тексте.
 * Порядок разрешения:
 * 1. Встроенные плейсхолдеры (player_name, server_online, ...)
 * 2. Плейсхолдеры LuckPerms (display_lp_*)
 * 3. PlaceholderAPI (все остальные %...%)
 *
 * Использует кэш с индивидуальными TTL для каждого плейсхолдера.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class PlaceholderResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    private final DarkRuneTab plugin;
    private final LuckPermsPlaceholders luckPermsPlaceholders;
    private final boolean papiAvailable;

    public PlaceholderResolver(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.luckPermsPlaceholders = new LuckPermsPlaceholders(plugin);
        this.papiAvailable = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Разрешает все плейсхолдеры в тексте для указанного игрока.
     *
     * @param player игрок
     * @param text исходный текст с %placeholders%
     * @return текст с разрешёнными плейсхолдерами
     */
    public String resolve(Player player, String text) {
        if (text == null || text.isEmpty() || player == null) {
            return text == null ? "" : text;
        }

        // Быстрая проверка: если нет %, возвращаем как есть
        if (text.indexOf('%') == -1) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length() + 32);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String resolved = resolveSingle(player, placeholder);
            matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Разрешает один плейсхолдер. Использует кэш.
     */
    private String resolveSingle(Player player, String placeholder) {
        Configs.PlaceholderConfig config = plugin.getConfigs().getPlaceholders();

        // Проверка чёрного списка
        if (config.isBlocked(placeholder)) {
            return "%" + placeholder + "%";
        }

        // Формируем ключ кэша
        String cacheKey = player.getUniqueId() + ":" + placeholder;

        // Проверяем кэш
        String cached = Caches.PLACEHOLDER_CACHE.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Разрешаем плейсхолдер
        String value = resolveDirect(player, placeholder);

        // Сохраняем в кэш
        Caches.PLACEHOLDER_CACHE.put(cacheKey, value);
        return value;
    }

    /**
     * Прямое разрешение плейсхолдера (без кэша).
     * Порядок: встроенные -> LuckPerms -> PlaceholderAPI
     */
    private String resolveDirect(Player player, String placeholder) {
        // 1. Встроенные плейсхолдеры
        String internal = InternalPlaceholders.resolve(player, placeholder);
        if (internal != null) {
            return internal;
        }

        // 2. Плейсхолдеры LuckPerms
        String lp = luckPermsPlaceholders.resolve(player, placeholder);
        if (lp != null) {
            return lp;
        }

        // 3. PlaceholderAPI
        if (papiAvailable) {
            try {
                String papiValue = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
                // Если PAPI не распознал - вернёт исходный текст с %%
                if (!papiValue.equals("%" + placeholder + "%")) {
                    return papiValue;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error resolving PAPI placeholder '" + placeholder + "': " + e.getMessage());
            }
        }

        // Не распознан - возвращаем как есть
        return "%" + placeholder + "%";
    }

    /**
     * Разрешает плейсхолдеры БЕЗ кэширования (для тестов/отладки).
     */
    public String resolveNoCache(Player player, String text) {
        if (text == null || text.isEmpty() || player == null) {
            return text == null ? "" : text;
        }

        if (text.indexOf('%') == -1) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length() + 32);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String resolved = resolveDirect(player, placeholder);
            matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Очищает кэш плейсхолдеров для конкретного игрока.
     */
    public void invalidatePlayerCache(Player player) {
        // Caffeine не поддерживает паттерн-удаление, поэтому инвалидируем весь кэш.
        // Для production можно сделать более точечный подход.
        Caches.PLACEHOLDER_CACHE.invalidateAll();
    }

    /**
     * Статистика кэша (для отладки).
     */
    public String getCacheStats() {
        return String.format("Placeholder cache: %d entries (hit rate: %.1f%%)",
                Caches.PLACEHOLDER_CACHE.estimatedSize(),
                Caches.PLACEHOLDER_CACHE.stats().hitRate() * 100);
    }
}