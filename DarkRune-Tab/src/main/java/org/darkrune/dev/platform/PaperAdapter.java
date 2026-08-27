package org.darkrune.dev.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.api.PlatformAdapter;
import org.darkrune.dev.api.TaskHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Адаптер для Paper.
 * Использует BukkitScheduler и виртуальные потоки Java 21.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class PaperAdapter implements PlatformAdapter {

    private final DarkRuneTab plugin;
    private final List<BukkitTask> activeTasks = new ArrayList<>();
    private final ConcurrentLinkedQueue<Player> updateQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean batchScheduled = false;

    private final Scoreboard sharedScoreboard;

    public PaperAdapter(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.sharedScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    @Override
    public void runAtPlayer(Player player, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runAsync(Runnable task) {
        Thread.startVirtualThread(task);
    }

    @Override
    public void runDelayed(Player player, Runnable task, long delayTicks) {
        BukkitTask handle = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        activeTasks.add(handle);
    }

    @Override
    public void runRepeating(Player player, Runnable task, long delayTicks, long periodTicks) {
        BukkitTask handle = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        activeTasks.add(handle);
    }

    @Override
    public TaskHandle runRepeatingGlobal(Runnable task, long delayTicks, long periodTicks) {
        BukkitTask handle = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        activeTasks.add(handle);
        return handle::cancel;
    }

    @Override
    public void sendTabListHeaderFooter(Player player, Component header, Component footer) {
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void updatePlayerDisplayName(Player viewer, Player target, Component displayName) {
        if (!viewer.isOnline() || !target.isOnline()) {
            return;
        }

        String teamName = getTeamName(target);
        Team team = sharedScoreboard.getTeam(teamName);

        if (team == null) {
            team = sharedScoreboard.registerNewTeam(teamName);
        }

        team.prefix(displayName);
        team.suffix(Component.empty());
        team.addEntry(target.getName());

        if (viewer.getScoreboard() != sharedScoreboard) {
            viewer.setScoreboard(sharedScoreboard);
        }
    }

    @Override
    public Scoreboard getSharedScoreboard() {
        return sharedScoreboard;
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

        Player player;
        while ((player = updateQueue.poll()) != null) {
            if (player.isOnline()) {
                plugin.getUpdateScheduler().processUpdate(player);
            }
        }
    }

    @Override
    public void cancelAllTasks() {
        activeTasks.forEach(BukkitTask::cancel);
        activeTasks.clear();
        updateQueue.clear();
    }

    @Override
    public String getPlatformName() {
        return "Paper";
    }

    private String getTeamName(Player player) {
        int weight = plugin.getLuckPerms().getGroupWeight(player.getUniqueId());
        return String.format("%04d_%s", Math.min(weight, 9999), player.getName());
    }
}