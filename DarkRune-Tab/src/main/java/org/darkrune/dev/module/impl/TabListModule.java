package org.darkrune.dev.module.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.api.TaskHandle;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Модуль таб-листа: хедер/футер + полный контроль строки игрока.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class TabListModule implements DisplayModule {

    private final DarkRuneTab plugin;
    private boolean enabled;
    private final List<TaskHandle> tasks = new ArrayList<>();

    public TabListModule(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigs().getTab().isEnabled();
    }

    @Override public String getName() { return "tablist"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public void initialize() {
        if (enabled) {
            startTasks();
        }
        plugin.getLogger().info("TabList module initialized");
    }

    @Override
    public void reload() {
        this.enabled = plugin.getConfigs().getTab().isEnabled();
        cancelTasks();
        if (enabled) {
            startTasks();
        }
    }

    @Override
    public void shutdown() {
        cancelTasks();
    }

    @Override
    public void resetAll() {
        // Сбрасываем всё, что модуль отправил клиентам
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getAdapter().runAtPlayer(player, () -> {
                player.playerListName(null);
                player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            });
        }
    }

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) return;
        updateHeaderFooter(player);
        applyPlayerFormat(player);
    }

    // ===== Задачи =====

    private void startTasks() {
        Configs.TabConfig config = plugin.getConfigs().getTab();

        tasks.add(plugin.getAdapter().runRepeatingGlobal(() -> {
            if (!enabled) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateHeaderFooter(p);
            }
        }, 20L, config.getHeaderUpdateInterval() * 20L));

        int playerInterval = config.getPlayerUpdateInterval();
        if (playerInterval > 0) {
            tasks.add(plugin.getAdapter().runRepeatingGlobal(() -> {
                if (!enabled) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    applyPlayerFormat(p);
                }
            }, 20L, playerInterval * 20L));
        }
    }

    private void cancelTasks() {
        tasks.forEach(TaskHandle::cancel);
        tasks.clear();
    }

    // ===== Обновления =====

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