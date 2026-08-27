package org.darkrune.dev.module.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.api.TaskHandle;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.render.Renderers;
import org.darkrune.dev.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Модуль боковой панели с анти-мерцанием.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class ScoreboardModule implements DisplayModule {

    private static final String OBJECTIVE_NAME = "darkrune_tab";

    private final DarkRuneTab plugin;
    private boolean enabled;
    private final List<TaskHandle> tasks = new ArrayList<>();
    private final Map<UUID, List<String>> lastLines = new ConcurrentHashMap<>();

    public ScoreboardModule(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigs().getScoreboard().isEnabled();
    }

    @Override public String getName() { return "scoreboard"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public void initialize() {
        if (enabled) {
            startTasks();
        }
        plugin.getLogger().info("Scoreboard module initialized");
    }

    @Override
    public void reload() {
        this.enabled = plugin.getConfigs().getScoreboard().isEnabled();
        cancelTasks();
        if (enabled) {
            startTasks();
        }
    }

    @Override
    public void shutdown() {
        cancelTasks();
        lastLines.clear();
    }

    @Override
    public void resetAll() {
        Scoreboard board = plugin.getAdapter().getSharedScoreboard();
        Objective objective = board.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }
        lastLines.clear();
    }

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) return;

        Configs.ScoreboardConfig config = plugin.getConfigs().getScoreboard();

        plugin.getAdapter().runAsync(() -> {
            String resolvedTitle = plugin.getPlaceholderResolver().resolve(player, config.getTitle());

            List<String> resolvedLines = new ArrayList<>();
            for (String line : config.getLines()) {
                resolvedLines.add(plugin.getPlaceholderResolver().resolve(player, line));
            }

            plugin.getAdapter().runAtPlayer(player, () -> {
                applyScoreboard(player, resolvedTitle, resolvedLines);
            });
        });
    }

    // ===== Задачи =====

    private void startTasks() {
        Configs.ScoreboardConfig config = plugin.getConfigs().getScoreboard();

        tasks.add(plugin.getAdapter().runRepeatingGlobal(() -> {
            if (!enabled) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                update(p);
            }
        }, 20L, config.getUpdateInterval() * 20L));
    }

    private void cancelTasks() {
        tasks.forEach(TaskHandle::cancel);
        tasks.clear();
    }

    // ===== Применение =====

    private void applyScoreboard(Player player, String title, List<String> lines) {
        Scoreboard board = plugin.getAdapter().getSharedScoreboard();

        Objective objective = board.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, Utils.parse(title));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        objective.displayName(Utils.parse(title));

        UUID uuid = player.getUniqueId();
        List<String> previous = lastLines.get(uuid);

        if (previous == null) {
            createAllLines(objective, lines);
        } else {
            updateChangedLines(board, objective, previous, lines);
        }

        lastLines.put(uuid, new ArrayList<>(lines));

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }

    private void createAllLines(Objective objective, List<String> lines) {
        int score = lines.size();
        for (String line : lines) {
            String uniqueLine = Renderers.formatScoreboardLine(score, line);
            objective.getScore(uniqueLine).setScore(score);
            score--;
        }
    }

    private void updateChangedLines(Scoreboard board, Objective objective,
                                    List<String> oldLines, List<String> newLines) {
        int maxSize = Math.max(oldLines.size(), newLines.size());

        for (int i = 0; i < maxSize; i++) {
            String oldLine = i < oldLines.size() ? oldLines.get(i) : null;
            String newLine = i < newLines.size() ? newLines.get(i) : null;

            int score = newLines.size() - i;

            if (oldLine == null && newLine != null) {
                String unique = Renderers.formatScoreboardLine(score, newLine);
                objective.getScore(unique).setScore(score);
            } else if (oldLine != null && newLine == null) {
                String oldUnique = Renderers.formatScoreboardLine(oldLines.size() - i, oldLine);
                board.resetScores(oldUnique);
            } else if (oldLine != null && !oldLine.equals(newLine)) {
                String oldUnique = Renderers.formatScoreboardLine(oldLines.size() - i, oldLine);
                String newUnique = Renderers.formatScoreboardLine(score, newLine);
                board.resetScores(oldUnique);
                objective.getScore(newUnique).setScore(score);
            }
        }
    }
}