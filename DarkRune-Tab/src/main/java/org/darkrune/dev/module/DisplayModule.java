package org.darkrune.dev.module;

import org.bukkit.entity.Player;

/**
 * Интерфейс для всех модулей отображения.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public interface DisplayModule {

    String getName();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void initialize();

    void shutdown();

    void update(Player player);

    /**
     * Перечитать конфиг, синхронизировать enabled
     * и перезапустить периодические задачи с новыми интервалами.
     */
    default void reload() {}

    /**
     * Сбросить видимые эффекты модуля (вызывается при toggle off).
     */
    default void resetAll() {}
}