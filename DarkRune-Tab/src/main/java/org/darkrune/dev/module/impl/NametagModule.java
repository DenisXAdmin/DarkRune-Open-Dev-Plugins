package org.darkrune.dev.module.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.util.Utils;

import java.util.ArrayList;

/**
 * Модуль неймтегов (имена над головой).
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

    @Override public String getName() { return "nametags"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public void initialize() {
        if (enabled) {
            plugin.getLogger().info("Nametag module initialized");
        }
    }

    @Override
    public void reload() {
        this.enabled = plugin.getConfigs().getNametag().isEnabled();
    }

    @Override
    public void shutdown() {}

    @Override
    public void resetAll() {
        // Удаляем все teams, созданные модулем, - имена возвращаются к ванильным
        Scoreboard board = plugin.getAdapter().getSharedScoreboard();
        for (Team team : new ArrayList<>(board.getTeams())) {
            team.unregister();
        }
    }

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) return;

        Configs.NametagConfig config = plugin.getConfigs().getNametag();

        String prefixText = config.getPrefix();
        String nameText = config.getName();
        String suffixText = config.getSuffix();

        plugin.getAdapter().runAsync(() -> {
            Component prefix = Utils.parse(plugin.getPlaceholderResolver().resolve(player, prefixText));
            Component name = Utils.parse(plugin.getPlaceholderResolver().resolve(player, nameText));
            Component suffix = Utils.parse(plugin.getPlaceholderResolver().resolve(player, suffixText));

            plugin.getAdapter().runAtPlayer(player, () -> {
                applyTeam(player, prefix, name, suffix);
            });
        });
    }

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

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }

    private String getTeamName(Player player) {
        int weight = plugin.getLuckPerms().getGroupWeight(player.getUniqueId());
        return String.format("%04d_%s", Math.min(weight, 9999), player.getName());
    }
}