package org.darkrune.dev.module;

import org.bukkit.entity.Player;

/**
 * Интерфейс для всех модулей отображения.
 */
public interface DisplayModule {

    String getName();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void initialize();

    void shutdown();

    void update(Player player);
}