package org.darkrune.dev.integration.luckperms;

/**
 * Неизменяемый контейнер мета-данных игрока из LuckPerms.
 * Кэшируется для снижения нагрузки на LuckPerms API.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public record MetaData(
        String prefix,
        String suffix,
        String primaryGroup,
        String primaryGroupDisplayName,
        String groupColor,
        int groupWeight
) {

    /**
     * Пустые мета-данные (если LuckPerms не ответил или игрок не найден).
     */
    public static MetaData empty() {
        return new MetaData("", "", "default", "Default", "WHITE", 0);
    }

    public boolean hasPrefix() {
        return prefix != null && !prefix.isEmpty();
    }

    public boolean hasSuffix() {
        return suffix != null && !suffix.isEmpty();
    }
}