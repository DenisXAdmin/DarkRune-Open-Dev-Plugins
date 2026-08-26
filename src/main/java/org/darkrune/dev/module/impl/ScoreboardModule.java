package org.darkrune.dev.module.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.darkrune.dev.DarkRuneTab;
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
 * Модуль боковой панели (scoreboard).
 * Использует анти-мерцание: обновляет только изменившиеся строки.
 * Работает на общем scoreboard, чтобы не конфликтовать с teams.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class ScoreboardModule implements DisplayModule {

    private static final String OBJECTIVE_NAME = "darkrune_tab";

    private final DarkRuneTab plugin;
    private boolean enabled;

    // Хранит последние строки для каждого игрока (анти-мерцание)
    private final Map<UUID, List<String>> lastLines = new ConcurrentHashMap<>();

    public ScoreboardModule(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigs().getScoreboard().isEnabled();
    }

    @Override
    public String getName() {
        return "scoreboard";
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

        Configs.ScoreboardConfig config = plugin.getConfigs().getScoreboard();
        long periodTicks = config.getUpdateInterval() * 20L;

        // Глобальная периодическая задача обновления scoreboard
        plugin.getAdapter().runRepeatingGlobal(() -> {
            if (!enabled) {
                return;
            }
            for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 20L, periodTicks);

        plugin.getLogger().info("Scoreboard module initialized (period: " + config.getUpdateInterval() + "s)");
    }

    @Override
    public void shutdown() {
        lastLines.clear();
    }

    @Override
    public void update(Player player) {
        if (!enabled || player == null || !player.isOnline()) {
            return;
        }

        Configs.ScoreboardConfig config = plugin.getConfigs().getScoreboard();

        // Асинхронное разрешение плейсхолдеров
        plugin.getAdapter().runAsync(() -> {
            String resolvedTitle = plugin.getPlaceholderResolver().resolve(player, config.getTitle());

            List<String> resolvedLines = new ArrayList<>();
            for (String line : config.getLines()) {
                String resolved = plugin.getPlaceholderResolver().resolve(player, line);
                resolvedLines.add(resolved);
            }

            // Применение в контексте игрока
            plugin.getAdapter().runAtPlayer(player, () -> {
                applyScoreboard(player, resolvedTitle, resolvedLines);
            });
        });
    }

    /**
     * Применяет scoreboard с анти-мерцанием.
     * Обновляет только изменившиеся строки.
     */
    private void applyScoreboard(Player player, String title, List<String> lines) {
        Scoreboard board = plugin.getAdapter().getSharedScoreboard();

        Objective objective = board.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, Utils.parse(title));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Обновляем заголовок
        objective.displayName(Utils.parse(title));

        UUID uuid = player.getUniqueId();
        List<String> previous = lastLines.get(uuid);

        if (previous == null) {
            // Первый раз - создаём все строки
            createAllLines(objective, lines);
        } else {
            // Обновляем только изменившиеся строки
            updateChangedLines(board, objective, previous, lines);
        }

        lastLines.put(uuid, new ArrayList<>(lines));

        // Устанавливаем scoreboard игроку
        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }

    /**
     * Создаёт все строки scoreboard (первое обновление).
     */
    private void createAllLines(Objective objective, List<String> lines) {
        int score = lines.size();
        for (String line : lines) {
            String uniqueLine = Renderers.formatScoreboardLine(score, line);
            objective.getScore(uniqueLine).setScore(score);
            score--;
        }
    }

    /**
     * Обновляет только изменившиеся строки (анти-мерцание).
     */
    private void updateChangedLines(Scoreboard board, Objective objective,
                                    List<String> oldLines, List<String> newLines) {
        int maxSize = Math.max(oldLines.size(), newLines.size());

        for (int i = 0; i < maxSize; i++) {
            String oldLine = i < oldLines.size() ? oldLines.get(i) : null;
            String newLine = i < newLines.size() ? newLines.get(i) : null;

            int score = newLines.size() - i;

            if (oldLine == null && newLine != null) {
                // Новая строка появилась
                String unique = Renderers.formatScoreboardLine(score, newLine);
                objective.getScore(unique).setScore(score);
            } else if (oldLine != null && newLine == null) {
                // Строка удалена
                String unique = Renderers.formatScoreboardLine(oldLines.size() - i, oldLine);
                board.resetScores(unique);
            } else if (oldLine != null && !oldLine.equals(newLine)) {
                // Строка изменилась
                String oldUnique = Renderers.formatScoreboardLine(oldLines.size() - i, oldLine);
                String newUnique = Renderers.formatScoreboardLine(score, newLine);
                board.resetScores(oldUnique);
                objective.getScore(newUnique).setScore(score);
            }
        }
    }
}