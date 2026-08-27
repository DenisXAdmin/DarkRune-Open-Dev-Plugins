package org.darkrune.dev.api;

/**
 * Дескриптор запущенной периодической задачи.
 * Позволяет модулям отменять свои задачи при reload/shutdown.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public interface TaskHandle {

    /**
     * Отменить задачу.
     */
    void cancel();
}