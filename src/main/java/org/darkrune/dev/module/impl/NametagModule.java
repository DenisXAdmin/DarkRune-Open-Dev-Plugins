package org.darkrune.dev.module.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.util.Utils;

/**
 * Модуль неймтегов.
 * Отвечает за префиксы, суффиксы и отображаемые имена
 * над головой и в таб-листе через scoreboard teams.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class NametagModule implements DisplayModule {

    private final DarkRuneTab plugin;
    private boolean enabled;

    public NametagModule(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigs().getNametag().isEnabled();
    }

    @Override
    public String getName() {
        return "nametags";
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
        plugin.getLogger().info("Nametag module initialized");
    }

    @Override
    public void shutdown() {
        // Очистка teams происходит при отключении плагина вместе со scoreboard
    }

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) {
            return;
        }

        Configs.NametagConfig config = plugin.getConfigs().getNametag();

        String prefixText = config.getPrefix();
        String nameText = config.getName();
        String suffixText = config.getSuffix();

        // Асинхронное разрешение плейсхолдеров
        plugin.getAdapter().runAsync(() -> {
            String resolvedPrefix = plugin.getPlaceholderResolver().resolve(player, prefixText);
            String resolvedName = plugin.getPlaceholderResolver().resolve(player, nameText);
            String resolvedSuffix = plugin.getPlaceholderResolver().resolve(player, suffixText);

            Component prefix = Utils.parse(resolvedPrefix);
            Component name = Utils.parse(resolvedName);
            Component suffix = Utils.parse(resolvedSuffix);

            // Обновление team в контексте игрока
            plugin.getAdapter().runAtPlayer(player, () -> {
                applyTeam(player, prefix, name, suffix);
            });
        });
    }

    /**
     * Применяет scoreboard team для игрока.
     * Вызывается в основном потоке / регионе игрока.
     */
    private void applyTeam(Player player, Component prefix, Component name, Component suffix) {
        Scoreboard board = plugin.getAdapter().getSharedScoreboard();

        String teamName = getTeamName(player);
        Team team = board.getTeam(teamName);

        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        team.prefix(prefix);
        team.suffix(suffix);

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        // Устанавливаем общий scoreboard игроку
        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }

    /**
     * Генерирует имя команды с учётом веса группы для сортировки.
     */
    private String getTeamName(Player player) {
        int weight = plugin.getLuckPerms().getGroupWeight(player.getUniqueId());
        return String.format("%04d_%s", Math.min(weight, 9999), player.getName());
    }
}