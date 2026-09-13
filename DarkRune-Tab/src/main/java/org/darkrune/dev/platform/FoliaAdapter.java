package org.darkrune.dev.platform;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.api.PlatformAdapter;
import org.darkrune.dev.api.TaskHandle;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Адаптер для Folia.
 * Использует нативный EntityScheduler для региональной привязки.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class FoliaAdapter implements PlatformAdapter {

    private final DarkRuneTab plugin;
    private final List<ScheduledTask> activeTasks = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedQueue<Player> updateQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean batchScheduled = false;

    private volatile Scoreboard sharedScoreboard;
    private volatile boolean scoreboardAttempted = false;
    private volatile boolean scoreboardSupported = false;

    public FoliaAdapter(DarkRuneTab plugin) {
        this.plugin = plugin;
        // Ленивая инициализация — Folia не позволяет создавать общий
        // scoreboard до полной инициализации сервера
    }

    /**
     * Ленивая инициализация shared scoreboard.
     * На Folia getNewScoreboard() бросает UnsupportedOperationException —
     * в этом случае работаем без общего скорборда (scoreboardSupported = false).
     */
    @Override
    public Scoreboard getSharedScoreboard() {
        if (!scoreboardAttempted) {
            synchronized (this) {
                if (!scoreboardAttempted) {
                    scoreboardAttempted = true;
                    try {
                        sharedScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                        scoreboardSupported = true;
                    } catch (UnsupportedOperationException e) {
                        // Folia не поддерживает общий scoreboard
                        sharedScoreboard = null;
                        scoreboardSupported = false;
                        plugin.getLogger().warning(
                                "Shared scoreboard is not supported on this platform. "
                                        + "Nametags and Scoreboard modules will be disabled.");
                    } catch (Exception e) {
                        sharedScoreboard = null;
                        scoreboardSupported = false;
                        plugin.getLogger().warning(
                                "Failed to create shared scoreboard: " + e.getMessage());
                    }
                }
            }
        }
        return sharedScoreboard;
    }

    /**
     * Проверка: поддерживается ли общий scoreboard на этой платформе.
     * Модули nametag/scoreboard должны это учитывать.
     */
    public boolean isScoreboardSupported() {
        if (!scoreboardAttempted) {
            getSharedScoreboard(); // триггерим ленивую инициализацию
        }
        return scoreboardSupported;
    }

    @Override
    public void runAtPlayer(Player player, Runnable task) {
        if (!player.isOnline()) return;
        EntityScheduler scheduler = player.getScheduler();
        scheduler.run(plugin, st -> task.run(), null);
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, st -> task.run());
    }

    @Override
    public void runDelayed(Player player, Runnable task, long delayTicks) {
        if (!player.isOnline()) return;
        EntityScheduler scheduler = player.getScheduler();
        ScheduledTask handle = scheduler.runDelayed(plugin, st -> task.run(), null, delayTicks);
        activeTasks.add(handle);
    }

    @Override
    public void runRepeating(Player player, Runnable task, long delayTicks, long periodTicks) {
        if (!player.isOnline()) return;
        EntityScheduler scheduler = player.getScheduler();
        ScheduledTask handle = scheduler.runAtFixedRate(plugin, st -> task.run(), null, delayTicks, periodTicks);
        activeTasks.add(handle);
    }

    @Override
    public TaskHandle runRepeatingGlobal(Runnable task, long delayTicks, long periodTicks) {
        ScheduledTask handle = Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, st -> task.run(), delayTicks, periodTicks);
        activeTasks.add(handle);
        return handle::cancel;
    }

    @Override
    public void sendTabListHeaderFooter(Player player, Component header, Component footer) {
        runAtPlayer(player, () -> player.sendPlayerListHeaderAndFooter(header, footer));
    }

    @Override
    public void updatePlayerDisplayName(Player viewer, Player target, Component displayName) {
        if (!viewer.isOnline() || !target.isOnline()) {
            return;
        }
        if (!isScoreboardSupported()) {
            // Fallback: используем displayName игрока напрямую
            target.displayName(displayName);
            return;
        }

        runAtPlayer(target, () -> {
            String teamName = getTeamName(target);
            Scoreboard board = sharedScoreboard;
            if (board == null) return;
            Team team = board.getTeam(teamName);

            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            team.prefix(displayName);
            team.suffix(Component.empty());
            team.addEntry(target.getName());

            runAtPlayer(viewer, () -> {
                if (viewer.getScoreboard() != board) {
                    viewer.setScoreboard(board);
                }
            });
        });
    }

    @Override
    public void queueUpdate(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        updateQueue.offer(player);

        if (!batchScheduled) {
            batchScheduled = true;
            long batchInterval = plugin.getConfigs().getPerformance().getBatchIntervalMs();

            runAsync(() -> {
                try {
                    Thread.sleep(batchInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                flushUpdates();
            });
        }
    }

    private void flushUpdates() {
        batchScheduled = false;

        Player current;
        while ((current = updateQueue.poll()) != null) {
            final Player player = current;
            if (player.isOnline()) {
                runAtPlayer(player, () -> plugin.getUpdateScheduler().processUpdate(player));
            }
        }
    }

    @Override
    public void cancelAllTasks() {
        activeTasks.forEach(ScheduledTask::cancel);
        activeTasks.clear();
        updateQueue.clear();
    }

    @Override
    public String getPlatformName() {
        return "Folia";
    }

    private String getTeamName(Player player) {
        int weight = plugin.getLuckPerms().getGroupWeight(player.getUniqueId());
        return String.format("%04d_%s", Math.min(weight, 9999), player.getName());
    }
}