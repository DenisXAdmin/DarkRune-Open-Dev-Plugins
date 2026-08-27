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

    private final Scoreboard sharedScoreboard;

    public FoliaAdapter(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.sharedScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
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

        runAtPlayer(target, () -> {
            String teamName = getTeamName(target);
            Team team = sharedScoreboard.getTeam(teamName);

            if (team == null) {
                team = sharedScoreboard.registerNewTeam(teamName);
            }

            team.prefix(displayName);
            team.suffix(Component.empty());
            team.addEntry(target.getName());

            runAtPlayer(viewer, () -> {
                if (viewer.getScoreboard() != sharedScoreboard) {
                    viewer.setScoreboard(sharedScoreboard);
                }
            });
        });
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