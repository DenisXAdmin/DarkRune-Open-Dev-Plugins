package org.darkrune.dev.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Общие утилиты плагина.
 * Содержит методы для работы с текстом, компонентами и коллекциями.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public final class Utils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND =
            LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION =
            LegacyComponentSerializer.legacySection();

    private Utils() {
        // Utility class - конструктор недоступен
    }

    // ===== Парсинг текста в Component =====

    /**
     * Парсит текст в Component с автоматическим определением формата.
     * Поддерживает: MiniMessage теги (<color>), legacy формат (& и секция).
     *
     * @param text исходный текст
     * @return готовый компонент
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Пробуем MiniMessage если есть теги
        if (text.contains("<") && text.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(text);
            } catch (Exception e) {
                // При ошибке парсинга пробуем legacy
                return parseLegacy(text);
            }
        }

        return parseLegacy(text);
    }

    /**
     * Парсит текст в legacy формате (& или секция).
     */
    private static Component parseLegacy(String text) {
        if (text.contains("&")) {
            return LEGACY_AMPERSAND.deserialize(text);
        }
        if (text.contains("\u00A7")) {
            return LEGACY_SECTION.deserialize(text);
        }
        return Component.text(text);
    }

    /**
     * Сериализует компонент обратно в строку (для отладки).
     */
    public static String serialize(Component component) {
        if (component == null) {
            return "";
        }
        return MINI_MESSAGE.serialize(component);
    }

    // ===== Работа с плейсхолдерами =====

    /**
     * Применяет карту плейсхолдеров к тексту.
     *
     * @param text исходный текст
     * @param placeholders карта замен (ключ -> значение)
     * @return текст с применёнными заменами
     */
    public static String applyPlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || text.isEmpty() || placeholders == null || placeholders.isEmpty()) {
            return text == null ? "" : text;
        }

        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // ===== Работа с текстом =====

    /**
     * Удаляет цветовые коды из текста.
     */
    public static String stripColor(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[&\u00A7][0-9a-fk-orx]", "");
    }

    /**
     * Проверяет наличие цветовых кодов в тексте.
     */
    public static boolean hasColorCodes(String text) {
        if (text == null) {
            return false;
        }
        return text.matches(".*[&\u00A7][0-9a-fk-orx].*");
    }

    /**
     * Обрезает строку до указанной длины с многоточием.
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    // ===== Работа с компонентами =====

    /**
     * Соединяет список компонентов с разделителем.
     */
    public static Component join(List<Component> components, Component separator) {
        if (components == null || components.isEmpty()) {
            return Component.empty();
        }

        Component result = components.get(0);
        for (int i = 1; i < components.size(); i++) {
            result = result.append(separator).append(components.get(i));
        }
        return result;
    }

    // ===== Проверки коллекций =====

    /**
     * Проверяет, что коллекция не пуста.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Проверяет, что строка не пуста и не состоит только из пробелов.
     */
    public static boolean isNotBlank(String text) {
        return text != null && !text.isBlank();
    }

    // ===== Форматирование времени =====

    /**
     * Форматирует секунды в читаемый вид.
     * Пример: 90 -> "1м 30с"
     */
    public static String formatSeconds(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes < 60) {
            return remainingSeconds > 0
                    ? String.format("%dm %ds", minutes, remainingSeconds)
                    : String.format("%dm", minutes);
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        return remainingMinutes > 0
                ? String.format("%dh %dm", hours, remainingMinutes)
                : String.format("%dh", hours);
    }
}