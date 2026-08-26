package org.darkrune.dev.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.darkrune.dev.DarkRuneTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Все конфигурации плагина DarkRune Tab.
 * Вложенные классы для каждого типа настроек.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class Configs {

    private final DarkRuneTab plugin;
    private TabConfig tab;
    private NametagConfig nametag;
    private ScoreboardConfig scoreboard;
    private MessagesConfig messages;
    private PerformanceConfig performance;
    private PlaceholderConfig placeholders;

    public Configs(DarkRuneTab plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public void loadAll() {
        FileConfiguration config = plugin.getConfig();
        this.tab = new TabConfig(config);
        this.nametag = new NametagConfig(config);
        this.scoreboard = new ScoreboardConfig(config);
        this.messages = new MessagesConfig(config);
        this.performance = new PerformanceConfig(config);
        this.placeholders = new PlaceholderConfig(config);
    }

    public void reloadAll() {
        plugin.reloadConfig();
        loadAll();
    }

    // ===== Getters =====
    public TabConfig getTab() { return tab; }
    public NametagConfig getNametag() { return nametag; }
    public ScoreboardConfig getScoreboard() { return scoreboard; }
    public MessagesConfig getMessages() { return messages; }
    public PerformanceConfig getPerformance() { return performance; }
    public PlaceholderConfig getPlaceholders() { return placeholders; }

    // ========================================================
    //  Вложенные классы конфигураций
    // ========================================================

    /**
     * Настройки таб-листа.
     */
    public static class TabConfig {
        private final boolean enabled;
        private final String header;
        private final String footer;
        private final int headerUpdateInterval;
        private final int footerUpdateInterval;
        private final String playerFormat;
        private final int playerUpdateInterval;
        private final boolean sortingEnabled;
        private final String sortingType;
        private final String sortingDirection;
        private final int maxVisiblePlayers;

        public TabConfig(FileConfiguration config) {
            this.enabled = config.getBoolean("modules.tablist.enabled", true);
            this.header = config.getString("modules.tablist.header.text", "");
            this.footer = config.getString("modules.tablist.footer.text", "");
            this.headerUpdateInterval = clamp(config.getInt("modules.tablist.header.update_interval", 5), 1, 600);
            this.footerUpdateInterval = clamp(config.getInt("modules.tablist.footer.update_interval", 5), 1, 600);
            this.playerFormat = config.getString("modules.tablist.player_format.format", "%player_name%");
            this.playerUpdateInterval = clamp(config.getInt("modules.tablist.player_format.update_interval", 0), 0, 600);
            this.sortingEnabled = config.getBoolean("modules.tablist.sorting.enabled", true);
            this.sortingType = config.getString("modules.tablist.sorting.type", "luckperms_weight");
            this.sortingDirection = config.getString("modules.tablist.sorting.direction", "descending");
            this.maxVisiblePlayers = clamp(config.getInt("modules.tablist.max_visible_players", 0), 0, 1000);
        }

        public boolean isEnabled() { return enabled; }
        public String getHeader() { return header; }
        public String getFooter() { return footer; }
        public int getHeaderUpdateInterval() { return headerUpdateInterval; }
        public int getFooterUpdateInterval() { return footerUpdateInterval; }
        public String getPlayerFormat() { return playerFormat; }
        public int getPlayerUpdateInterval() { return playerUpdateInterval; }
        public boolean isSortingEnabled() { return sortingEnabled; }
        public String getSortingType() { return sortingType; }
        public String getSortingDirection() { return sortingDirection; }
        public int getMaxVisiblePlayers() { return maxVisiblePlayers; }
    }

    /**
     * Настройки неймтегов.
     */
    public static class NametagConfig {
        private final boolean enabled;
        private final String prefix;
        private final String name;
        private final String suffix;
        private final boolean teamColorEnabled;
        private final Map<String, String> groupColors;

        public NametagConfig(FileConfiguration config) {
            this.enabled = config.getBoolean("modules.nametags.enabled", true);
            this.prefix = config.getString("modules.nametags.format.prefix", "%display_lp_prefix%");
            this.name = config.getString("modules.nametags.format.name", "%player_displayname%");
            this.suffix = config.getString("modules.nametags.format.suffix", "%display_lp_suffix%");
            this.teamColorEnabled = config.getBoolean("modules.nametags.team_color.enabled", true);

            this.groupColors = new HashMap<>();
            ConfigurationSection colors = config.getConfigurationSection("modules.nametags.team_color.colors");
            if (colors != null) {
                for (String key : colors.getKeys(false)) {
                    groupColors.put(key, colors.getString(key, "WHITE"));
                }
            }
        }

        public boolean isEnabled() { return enabled; }
        public String getPrefix() { return prefix; }
        public String getName() { return name; }
        public String getSuffix() { return suffix; }
        public boolean isTeamColorEnabled() { return teamColorEnabled; }
        public Map<String, String> getGroupColors() { return groupColors; }
    }

    /**
     * Настройки боковой панели (scoreboard).
     */
    public static class ScoreboardConfig {
        private final boolean enabled;
        private final String title;
        private final List<String> lines;
        private final int updateInterval;

        public ScoreboardConfig(FileConfiguration config) {
            this.enabled = config.getBoolean("modules.scoreboard.enabled", false);
            this.title = config.getString("modules.scoreboard.title", "");
            this.lines = new ArrayList<>(config.getStringList("modules.scoreboard.lines"));
            this.updateInterval = clamp(config.getInt("modules.scoreboard.update_interval", 5), 1, 600);
        }

        public boolean isEnabled() { return enabled; }
        public String getTitle() { return title; }
        public List<String> getLines() { return lines; }
        public int getUpdateInterval() { return updateInterval; }
    }

    /**
     * Сообщения плагина.
     */
    public static class MessagesConfig {
        private final String prefix;
        private final String reloaded;
        private final String noPermission;
        private final String moduleToggled;
        private final String moduleNotFound;
        private final String helpHeader;

        public MessagesConfig(FileConfiguration config) {
            this.prefix = config.getString("plugin.prefix", "&8[&5DarkRune Tab&8]&r ");
            this.reloaded = config.getString("messages.reloaded", "&aConfigs reloaded in &e%time%ms");
            this.noPermission = config.getString("messages.no_permission", "&cNo permission!");
            this.moduleToggled = config.getString("messages.module_toggled", "&aModule &e%module% &atoggled!");
            this.moduleNotFound = config.getString("messages.module_not_found", "&cModule not found: &e%module%");
            this.helpHeader = config.getString("messages.help_header", "&6=== DarkRune Tab Help ===");
        }

        public String getPrefix() { return prefix; }
        public String getReloaded() { return reloaded; }
        public String getNoPermission() { return noPermission; }
        public String getModuleToggled() { return moduleToggled; }
        public String getModuleNotFound() { return moduleNotFound; }
        public String getHelpHeader() { return helpHeader; }
    }

    /**
     * Настройки производительности.
     */
    public static class PerformanceConfig {
        private final boolean asyncResolution;
        private final boolean batchUpdates;
        private final long batchIntervalMs;
        private final int maxUpdatesPerSecond;
        private final int componentCacheSize;
        private final int placeholderCacheSize;

        public PerformanceConfig(FileConfiguration config) {
            this.asyncResolution = config.getBoolean("performance.async_resolution", true);
            this.batchUpdates = config.getBoolean("performance.batch_updates", true);
            this.batchIntervalMs = clamp(config.getLong("performance.batch_interval_ms", 100), 10, 5000);
            this.maxUpdatesPerSecond = clamp(config.getInt("performance.max_updates_per_second", 5), 1, 100);
            this.componentCacheSize = clamp(config.getInt("performance.component_cache_size", 10000), 100, 100000);
            this.placeholderCacheSize = clamp(config.getInt("performance.placeholder_cache_size", 50000), 1000, 500000);
        }

        public boolean isAsyncResolution() { return asyncResolution; }
        public boolean isBatchUpdates() { return batchUpdates; }
        public long getBatchIntervalMs() { return batchIntervalMs; }
        public int getMaxUpdatesPerSecond() { return maxUpdatesPerSecond; }
        public int getComponentCacheSize() { return componentCacheSize; }
        public int getPlaceholderCacheSize() { return placeholderCacheSize; }
    }

    /**
     * Настройки плейсхолдеров (TTL для кэширования).
     */
    public static class PlaceholderConfig {
        private final int defaultUpdateInterval;
        private final Map<String, Integer> ttlOverrides;
        private final boolean blacklistEnabled;
        private final List<String> blacklist;

        public PlaceholderConfig(FileConfiguration config) {
            this.defaultUpdateInterval = clamp(config.getInt("placeholders.default_update_interval", 10), 0, 3600);
            this.ttlOverrides = new HashMap<>();
            ConfigurationSection ttl = config.getConfigurationSection("placeholders.ttl");
            if (ttl != null) {
                for (String key : ttl.getKeys(false)) {
                    ttlOverrides.put(key, ttl.getInt(key, defaultUpdateInterval));
                }
            }
            this.blacklistEnabled = config.getBoolean("placeholders.blacklist.enabled", false);
            this.blacklist = new ArrayList<>(config.getStringList("placeholders.blacklist.blocked"));
        }

        public int getDefaultUpdateInterval() { return defaultUpdateInterval; }
        public int getTTL(String placeholder) {
            return ttlOverrides.getOrDefault(placeholder, defaultUpdateInterval);
        }
        public boolean isBlacklistEnabled() { return blacklistEnabled; }
        public boolean isBlocked(String placeholder) {
            return blacklistEnabled && blacklist.contains(placeholder);
        }
    }

    /**
     * Утилита ограничения числа в диапазоне.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}