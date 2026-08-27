package org.darkrune.dev.integration.luckperms;

import org.bukkit.entity.Player;
import org.darkrune.dev.DarkRuneTab;

/**
 * Встроенные плейсхолдеры LuckPerms.
 * Регистрирует и разрешает плейсхолдеры вида display_lp_*.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class LuckPermsPlaceholders {

    private final DarkRuneTab plugin;

    public LuckPermsPlaceholders(DarkRuneTab plugin) {
        this.plugin = plugin;
    }

    /**
     * Разрешает плейсхолдер вида "display_lp_*".
     * Возвращает null, если плейсхолдер не распознан.
     *
     * @param player игрок
     * @param placeholder имя плейсхолдера (без %%)
     * @return значение или null
     */
    public String resolve(Player player, String placeholder) {
        if (player == null || placeholder == null) {
            return null;
        }

        // Обрабатываем только наши плейсхолдеры
        if (!placeholder.startsWith("display_lp_")) {
            return null;
        }

        LuckPermsIntegration lp = plugin.getLuckPerms();
        if (lp == null) {
            return "";
        }

        return switch (placeholder) {
            case "display_lp_prefix" -> lp.getPrefix(player.getUniqueId());
            case "display_lp_suffix" -> lp.getSuffix(player.getUniqueId());
            case "display_lp_primary_group" -> lp.getPrimaryGroup(player.getUniqueId());
            case "display_lp_primary_group_displayname" -> lp.getPrimaryGroupDisplayName(player.getUniqueId());
            case "display_lp_group_weight" -> String.valueOf(lp.getGroupWeight(player.getUniqueId()));
            case "display_lp_group_color" -> lp.getGroupColor(player.getUniqueId());
            default -> resolveMetaPlaceholder(player, placeholder, lp);
        };
    }

    /**
     * Разрешает плейсхолдеры вида display_lp_meta_<key>.
     */
    private String resolveMetaPlaceholder(Player player, String placeholder, LuckPermsIntegration lp) {
        if (placeholder.startsWith("display_lp_meta_")) {
            String key = placeholder.substring("display_lp_meta_".length());
            if (!key.isEmpty()) {
                String value = lp.getMetaValue(player.getUniqueId(), key);
                return value != null ? value : "";
            }
        }

        if (placeholder.startsWith("display_lp_has_group_")) {
            String groupName = placeholder.substring("display_lp_has_group_".length());
            String primary = lp.getPrimaryGroup(player.getUniqueId());
            return String.valueOf(groupName.equalsIgnoreCase(primary));
        }

        return null;
    }
}