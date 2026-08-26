package org.darkrune.dev.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.cache.Caches;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Главная команда /darkrunetab.
 *
 * Подкоманды:
 * - /darkrunetab help     - показать справку
 * - /darkrunetab reload   - перезагрузить конфиги
 * - /darkrunetab toggle   - переключить модуль
 * - /darkrunetab debug    - отладочная информация
 * - /darkrunetab stats    - статистика кэша
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class TabCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("help", "reload", "toggle", "debug", "stats");
    private static final List<String> TOGGABLE_MODULES = List.of("tablist", "nametags", "scoreboard");

    private final DarkRuneTab plugin;

    public TabCommand(DarkRuneTab plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            handleHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        return switch (sub) {
            case "reload" -> handleReload(sender);
            case "toggle" -> handleToggle(sender, args);
            case "debug" -> handleDebug(sender);
            case "stats" -> handleStats(sender);
            case "help" -> {
                handleHelp(sender);
                yield true;
            }
            default -> {
                sendMessage(sender, "&cUnknown subcommand. Use &e/darkrunetab help");
                yield true;
            }
        };
    }

    /**
     * /darkrunetab help
     */
    private void handleHelp(CommandSender sender) {
        Configs.MessagesConfig msg = plugin.getConfigs().getMessages();

        sendMessage(sender, msg.getHelpHeader());
        sendMessage(sender, "&7Version: &e" + plugin.getDescription().getVersion());
        sendMessage(sender, "");
        sendMessage(sender, "&e/darkrunetab help    &7- show this help");
        sendMessage(sender, "&e/darkrunetab reload  &7- reload all configurations");
        sendMessage(sender, "&e/darkrunetab toggle  &7- toggle a display module");
        sendMessage(sender, "&e/darkrunetab debug   &7- show debug information");
        sendMessage(sender, "&e/darkrunetab stats   &7- show cache statistics");
    }

    /**
     * /darkrunetab reload
     */
    private boolean handleReload(CommandSender sender) {
        if (!checkPermission(sender, "darkrunetab.reload")) {
            return true;
        }

        long startTime = System.currentTimeMillis();

        plugin.getConfigs().reloadAll();
        Caches.clearAll();

        long duration = System.currentTimeMillis() - startTime;

        String message = plugin.getConfigs().getMessages().getReloaded()
                .replace("%time%", String.valueOf(duration));
        sendMessage(sender, message);

        plugin.getLogger().info("Configurations reloaded by " + sender.getName()
                + " in " + duration + "ms");
        return true;
    }

    /**
     * /darkrunetab toggle <module>
     */
    private boolean handleToggle(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "darkrunetab.toggle")) {
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "&cUsage: &e/darkrunetab toggle <module>");
            sendMessage(sender, "&7Available: &e" + String.join(", ", TOGGABLE_MODULES));
            return true;
        }

        String moduleName = args[1].toLowerCase();
        DisplayModule module = plugin.getModuleManager().getModule(moduleName);

        if (module == null) {
            String message = plugin.getConfigs().getMessages().getModuleNotFound()
                    .replace("%module%", moduleName);
            sendMessage(sender, message);
            return true;
        }

        boolean newState = !module.isEnabled();
        module.setEnabled(newState);

        String message = plugin.getConfigs().getMessages().getModuleToggled()
                .replace("%module%", moduleName)
                .replace("%state%", newState ? "&aenabled" : "&cdisabled");
        sendMessage(sender, message);

        plugin.getLogger().info("Module " + moduleName + " " + (newState ? "enabled" : "disabled")
                + " by " + sender.getName());
        return true;
    }

    /**
     * /darkrunetab debug
     */
    private boolean handleDebug(CommandSender sender) {
        if (!checkPermission(sender, "darkrunetab.debug")) {
            return true;
        }

        sendMessage(sender, "&6=== DarkRune Tab Debug ===");
        sendMessage(sender, "&7Plugin: &e" + plugin.getDescription().getFullName());
        sendMessage(sender, "&7Platform: &e" + plugin.getPlatform().getDisplayName());
        sendMessage(sender, "&7Adapter: &e" + plugin.getAdapter().getPlatformName());
        sendMessage(sender, "&7Online players: &e" + plugin.getServer().getOnlinePlayers().size());
        sendMessage(sender, "&7Max players: &e" + plugin.getServer().getMaxPlayers());
        sendMessage(sender, "");
        sendMessage(sender, "&6Active modules:");

        for (DisplayModule module : plugin.getModuleManager().getActiveModules()) {
            sendMessage(sender, "  &a- &e" + module.getName());
        }

        sendMessage(sender, "");
        sendMessage(sender, "&6Disabled modules:");

        for (DisplayModule module : plugin.getModuleManager().getDisabledModules()) {
            sendMessage(sender, "  &c- &e" + module.getName());
        }

        return true;
    }

    /**
     * /darkrunetab stats
     */
    private boolean handleStats(CommandSender sender) {
        if (!checkPermission(sender, "darkrunetab.debug")) {
            return true;
        }

        sendMessage(sender, "&6=== Cache Statistics ===");
        sendMessage(sender, "&7" + Caches.getStats());
        sendMessage(sender, "");
        sendMessage(sender, "&7LuckPerms: &e" + plugin.getLuckPerms().getCacheStats());
        sendMessage(sender, "&7Placeholders: &e" + plugin.getPlaceholderResolver().getCacheStats());
        sendMessage(sender, "&7Active updates: &e" + plugin.getUpdateScheduler().getActivePlayerCount());

        return true;
    }

    /**
     * Проверка права с выводом сообщения об отказе.
     */
    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sendMessage(sender, plugin.getConfigs().getMessages().getNoPermission());
            return false;
        }
        return true;
    }

    /**
     * Отправка сообщения с префиксом и парсингом цветов.
     */
    private void sendMessage(CommandSender sender, String message) {
        String prefix = plugin.getConfigs().getMessages().getPrefix();
        Component component = Utils.parse(prefix + message);
        sender.sendMessage(component);
    }

    // ===== Tab completion =====

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("darkrunetab.use")) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("darkrunetab.toggle")) {
                return List.of();
            }
            return filter(TOGGABLE_MODULES, args[1]);
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase().startsWith(lower)) {
                result.add(opt);
            }
        }
        return result;
    }
}