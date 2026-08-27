package org.darkrune.dev;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.darkrune.dev.api.PlatformAdapter;
import org.darkrune.dev.api.PlatformDetector;
import org.darkrune.dev.api.PlatformFactory;
import org.darkrune.dev.cache.Caches;
import org.darkrune.dev.command.TabCommand;
import org.darkrune.dev.config.Configs;
import org.darkrune.dev.integration.luckperms.LuckPermsIntegration;
import org.darkrune.dev.listener.Listeners;
import org.darkrune.dev.module.ModuleManager;
import org.darkrune.dev.placeholder.PlaceholderResolver;
import org.darkrune.dev.render.UpdateScheduler;

/**
 * Главный класс плагина DarkRune Tab.
 * Высокопроизводительный Tab List для Paper 1.21+.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class DarkRuneTab extends JavaPlugin {

    private static DarkRuneTab instance;

    private PlatformDetector.Platform platform;
    private PlatformAdapter adapter;
    private Configs configs;
    private LuckPermsIntegration luckPerms;
    private PlaceholderResolver placeholderResolver;
    private UpdateScheduler updateScheduler;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;

        // Сохраняем дефолтный конфиг при первом запуске
        saveDefaultConfig();

        // Определяем платформу и создаём адаптер (один раз при старте)
        this.platform = PlatformDetector.detect();
        this.adapter = PlatformFactory.createAdapter(this);

        getLogger().info("Detected platform: " + platform);
        getLogger().info("Using adapter: " + adapter.getPlatformName());

        // Проверка зависимостей (обе опциональны - плагин работает и без них)
        checkDependencies();

        // Инициализация компонентов
        initializeComponents();

        // Регистрация команд
        registerCommands();

        // Регистрация слушателей
        Listeners.registerAll(this);

        getLogger().info("DarkRune Tab v" + getDescription().getVersion()
                + " enabled on " + platform + "!");
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.shutdown();
        }

        if (updateScheduler != null) {
            updateScheduler.shutdown();
        }

        if (adapter != null) {
            adapter.cancelAllTasks();
        }

        Caches.clearAll();

        getLogger().info("DarkRune Tab disabled.");
    }

    /**
     * Проверка зависимостей.
     * Обе зависимости опциональны:
     * - Без LuckPerms отключаются префиксы/суффиксы/сортировка по весу группы
     * - Без PlaceholderAPI часть внешних плейсхолдеров не будет работать
     * Плагин запускается в любом случае.
     */
    private void checkDependencies() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().warning("LuckPerms not found. Prefixes, suffixes and group sorting will be unavailable.");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("PlaceholderAPI not found. Some placeholders will not work.");
        }
    }

    /**
     * Инициализация всех компонентов плагина.
     */
    private void initializeComponents() {
        this.configs = new Configs(this);
        this.luckPerms = new LuckPermsIntegration(this);
        this.placeholderResolver = new PlaceholderResolver(this);
        this.updateScheduler = new UpdateScheduler(this);
        this.moduleManager = new ModuleManager(this);
        this.moduleManager.initialize();
    }

    /**
     * Регистрация основной команды.
     */
    private void registerCommands() {
        TabCommand command = new TabCommand(this);

        var commandObj = getCommand("darkrunetab");
        if (commandObj != null) {
            commandObj.setExecutor(command);
            commandObj.setTabCompleter(command);
        }
    }

    // ===== Getters =====

    public static DarkRuneTab getInstance() {
        return instance;
    }

    public PlatformDetector.Platform getPlatform() {
        return platform;
    }

    public PlatformAdapter getAdapter() {
        return adapter;
    }

    public Configs getConfigs() {
        return configs;
    }

    public LuckPermsIntegration getLuckPerms() {
        return luckPerms;
    }

    public PlaceholderResolver getPlaceholderResolver() {
        return placeholderResolver;
    }

    public UpdateScheduler getUpdateScheduler() {
        return updateScheduler;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}