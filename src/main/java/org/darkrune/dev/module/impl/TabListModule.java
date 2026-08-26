package org.darkrune.dev.module.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.util.Utils;

/**
 * Модуль таб-листа.
 * Хедер/футер + полный контроль строки игрока в табе (player_format).
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class TabListModule implements DisplayModule {

    private final DarkRuneTab plugin;
    private boolean enabled;

    public TabListModule(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigs().getTab().isEnabled();
    }

    @Override public String getName() { return "tablist"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean e) { this.enabled = e; }

    @Override
    public void initialize() {
        if (!enabled) return;

        Configs.TabConfig config = plugin.getConfigs().getTab();
        long headerPeriod = config.getHeaderUpdateInterval() * 20L;

        // Периодическое обновление хедера/футера
        plugin.getAdapter().runRepeatingGlobal(() -> {
            if (!enabled) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateHeaderFooter(p);
            }
        }, 20L, headerPeriod);

        // Периодическое обновление формата игрока (если interval > 0)
        int playerInterval = config.getPlayerUpdateInterval();
        if (playerInterval > 0) {
            long playerPeriod = playerInterval * 20L;
            plugin.getAdapter().runRepeatingGlobal(() -> {
                if (!enabled) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    applyPlayerFormat(p);
                }
            }, 20L, playerPeriod);
        }

        plugin.getLogger().info("TabList module initialized");
    }

    @Override public void shutdown() {}

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) return;
        updateHeaderFooter(player);
        applyPlayerFormat(player);
    }

    private void updateHeaderFooter(Player player) {
        Configs.TabConfig config = plugin.getConfigs().getTab();
        String headerText = config.getHeader();
        String footerText = config.getFooter();
        if (headerText.isEmpty() && footerText.isEmpty()) return;

        plugin.getAdapter().runAsync(() -> {
            Component header = Utils.parse(plugin.getPlaceholderResolver().resolve(player, headerText));
            Component footer = Utils.parse(plugin.getPlaceholderResolver().resolve(player, footerText));
            plugin.getAdapter().sendTabListHeaderFooter(player, header, footer);
        });
    }

    /**
     * Применяет tablist.player_format как имя игрока в табе.
     * Даёт полный контроль над строкой в табе (ники, HP, титулы).
     */
    private void applyPlayerFormat(Player player) {
        String format = plugin.getConfigs().getTab().getPlayerFormat();
        if (format == null || format.isEmpty()) {
            player.playerListName(null);
            return;
        }

        plugin.getAdapter().runAsync(() -> {
            Component name = Utils.parse(plugin.getPlaceholderResolver().resolve(player, format));
            plugin.getAdapter().runAtPlayer(player, () -> {
                if (player.isOnline()) {
                    player.playerListName(name);
                }
            });
        });
    }
}
