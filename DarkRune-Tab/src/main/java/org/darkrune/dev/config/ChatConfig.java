package org.darkrune.dev.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Конфигурация форматирования чата.
 * Поддерживает приоритетные форматы по правам и фолбэк-формат.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class ChatConfig {

    /**
     * Один формат чата, привязанный к праву.
     */
    public static class ChatFormat {
        private final String name;
        private final String permission;
        private final int priority;
        private final String format;

        public ChatFormat(String name, String permission, int priority, String format) {
            this.name = name;
            this.permission = permission;
            this.priority = priority;
            this.format = format;
        }

        public String getName() { return name; }
        public String getPermission() { return permission; }
        public int getPriority() { return priority; }
        public String getFormat() { return format; }
    }

    /**
     * Проверка права (чтобы не тащить зависимость от плагина в конфиг).
     */
    public interface PermissionChecker {
        boolean has(String permission);
    }

    private final boolean enabled;
    private final String defaultFormat;
    private final boolean stripPlayerColors;
    private final String colorPermission;
    private final List<ChatFormat> formats;

    public ChatConfig(FileConfiguration config) {
        this.enabled = config.getBoolean("modules.chat.enabled", true);
        this.defaultFormat = config.getString("modules.chat.default_format",
                "%display_lp_prefix%%player_name%%display_lp_suffix%&7: &f%message%");
        this.stripPlayerColors = config.getBoolean("modules.chat.strip_player_colors", true);
        this.colorPermission = config.getString("modules.chat.color_permission",
                "darkrunetab.chat.colors");

        this.formats = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("modules.chat.formats");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String permission = section.getString(key + ".permission", "");
                int priority = section.getInt(key + ".priority", 0);
                String format = section.getString(key + ".format", defaultFormat);
                formats.add(new ChatFormat(key, permission, priority, format));
            }
        }
        // Сортировка: больший приоритет проверяется первым
        formats.sort(Comparator.comparingInt(ChatFormat::getPriority).reversed());
    }

    /**
     * Возвращает формат для игрока: первый, чьё право совпадает.
     * Если ни одно не совпало - defaultFormat.
     */
    public String getFormatFor(PermissionChecker checker) {
        for (ChatFormat format : formats) {
            if (!format.getPermission().isEmpty() && checker.has(format.getPermission())) {
                return format.getFormat();
            }
        }
        return defaultFormat;
    }

    public boolean isEnabled() { return enabled; }
    public String getDefaultFormat() { return defaultFormat; }
    public boolean isStripPlayerColors() { return stripPlayerColors; }
    public String getColorPermission() { return colorPermission; }
    public List<ChatFormat> getFormats() { return formats; }
}