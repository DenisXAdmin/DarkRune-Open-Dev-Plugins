package org.darkrune.dev.module;

import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.module.impl.ChatModule;
import org.darkrune.dev.module.impl.NametagModule;
import org.darkrune.dev.module.impl.ScoreboardModule;
import org.darkrune.dev.module.impl.TabListModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер всех модулей отображения.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class ModuleManager {

    private final DarkRuneTab plugin;
    private final Map<String, DisplayModule> modules = new LinkedHashMap<>();

    public ModuleManager(DarkRuneTab plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        registerModule(new TabListModule(plugin));
        registerModule(new NametagModule(plugin));
        registerModule(new ScoreboardModule(plugin));
        registerModule(new ChatModule(plugin));

        for (DisplayModule module : modules.values()) {
            if (module.isEnabled()) {
                try {
                    module.initialize();
                    plugin.getLogger().info("Module initialized: " + module.getName());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to initialize module "
                            + module.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Перезагрузка всех модулей: синхронизация enabled
     * и перезапуск задач с новыми интервалами.
     */
    public void reload() {
        for (DisplayModule module : modules.values()) {
            try {
                module.reload();
            } catch (Exception e) {
                plugin.getLogger().warning("Error reloading module "
                        + module.getName() + ": " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        for (DisplayModule module : modules.values()) {
            try {
                module.shutdown();
            } catch (Exception e) {
                plugin.getLogger().warning("Error shutting down module "
                        + module.getName() + ": " + e.getMessage());
            }
        }
    }

    private void registerModule(DisplayModule module) {
        modules.put(module.getName().toLowerCase(), module);
    }

    public boolean toggle(String moduleName) {
        DisplayModule module = modules.get(moduleName.toLowerCase());
        if (module == null) {
            return false;
        }
        module.setEnabled(!module.isEnabled());
        return true;
    }

    public DisplayModule getModule(String name) {
        return modules.get(name.toLowerCase());
    }

    public Collection<DisplayModule> getAllModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public List<DisplayModule> getActiveModules() {
        List<DisplayModule> result = new ArrayList<>();
        for (DisplayModule module : modules.values()) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }

    public List<DisplayModule> getDisabledModules() {
        List<DisplayModule> result = new ArrayList<>();
        for (DisplayModule module : modules.values()) {
            if (!module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }

    public String getActiveModuleNames() {
        List<DisplayModule> active = getActiveModules();
        if (active.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (DisplayModule module : active) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(module.getName());
        }
        return sb.toString();
    }
}