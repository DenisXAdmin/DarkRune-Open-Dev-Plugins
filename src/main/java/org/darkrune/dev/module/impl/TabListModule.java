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
 * Отвечает за хедер и футер таб-листа.
 * Обновляется периодически и при событиях.
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

    @Override
    public String getName() {
        return "tablist";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void initialize() {
        if (!enabled) {
            return;
        }

        Configs.TabConfig config = plugin.getConfigs().getTab();
        long periodTicks = config.getHeaderUpdateInterval() * 20L;

        // Глобальная периодическая задача обновления хедера/футера
        plugin.getAdapter().runRepeatingGlobal(() -> {
            if (!enabled) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateHeaderFooter(player);
            }
        }, 20L, periodTicks);

        plugin.getLogger().info("TabList module initialized (period: " + config.getHeaderUpdateInterval() + "s)");
    }

    @Override
    public void shutdown() {
        // Задачи отменяются глобально через adapter.cancelAllTasks()
    }

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) {
            return;
        }
        updateHeaderFooter(player);
    }

    /**
     * Обновляет хедер и футер таб-листа для игрока.
     * Разрешение плейсхолдеров выполняется асинхронно.
     */
    private void updateHeaderFooter(Player player) {
        Configs.TabConfig config = plugin.getConfigs().getTab();

        String headerText = config.getHeader();
        String footerText = config.getFooter();

        // Если оба пустые - нечего отправлять
        if (headerText.isEmpty() && footerText.isEmpty()) {
            return;
        }

        // Асинхронное разрешение плейсхолдеров
        plugin.getAdapter().runAsync(() -> {
            String resolvedHeader = plugin.getPlaceholderResolver().resolve(player, headerText);
            String resolvedFooter = plugin.getPlaceholderResolver().resolve(player, footerText);

            Component header = Utils.parse(resolvedHeader);
            Component footer = Utils.parse(resolvedFooter);

            // Отправка в контексте игрока (учитывает регион в Folia)
            plugin.getAdapter().sendTabListHeaderFooter(player, header, footer);
        });
    }
}