package org.darkrune.dev.render;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.darkrune.dev.util.Utils;

/**
 * Утилиты для рендеринга элементов отображения.
 * Содержит методы форматирования строк, сборки компонентов и т.д.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public final class Renderers {

    private Renderers() {
    }

    /**
     * Форматирует строку scoreboard'а с номером.
     * Minecraft требует уникальные строки для каждой линии.
     *
     * @param lineIndex индекс строки (0-15)
     * @param content содержимое строки
     * @return уникальная строка для scoreboard
     */
    public static String formatScoreboardLine(int lineIndex, String content) {
        // Используем невидимые символы для уникальности строк
        // Это предотвращает мерцание при обновлении
        StringBuilder unique = new StringBuilder();
        for (int i = 0; i < lineIndex; i++) {
            unique.append('\u00A7').append('r');
        }
        return unique + content;
    }

    /**
     * Создаёт компонент для заголовка scoreboard'а.
     *
     * @param title текст заголовка
     * @return компонент
     */
    public static Component createScoreboardTitle(String title) {
        return Utils.parse(title);
    }

    /**
     * Создаёт компонент для строки scoreboard'а.
     *
     * @param line текст строки
     * @return компонент
     */
    public static Component createScoreboardLine(String line) {
        return Utils.parse(line);
    }

    /**
     * Парсит цвет из строки (например, "RED", "BLUE", "#FF0000").
     *
     * @param colorName название цвета
     * @return NamedTextColor или WHITE по умолчанию
     */
    public static TextColor parseColor(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return NamedTextColor.WHITE;
        }

        // Пробуем как hex
        if (colorName.startsWith("#")) {
            try {
                return TextColor.fromHexString(colorName);
            } catch (Exception e) {
                return NamedTextColor.WHITE;
            }
        }

        // Пробуем как NamedTextColor
        try {
            return NamedTextColor.NAMES.value(colorName.toLowerCase());
        } catch (Exception e) {
            return NamedTextColor.WHITE;
        }
    }

    /**
     * Создаёт пустую строку для scoreboard'а.
     */
    public static String emptyLine() {
        return " ";
    }

    /**
     * Создаёт разделитель для scoreboard'а.
     *
     * @param length длина разделителя
     * @param color цвет
     * @return строка-разделитель
     */
    public static String createSeparator(int length, String color) {
        StringBuilder sb = new StringBuilder();
        sb.append(color);
        for (int i = 0; i < length; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    /**
     * Ограничивает длину строки для scoreboard'а (макс 32 символа).
     *
     * @param text исходный текст
     * @return обрезанный текст
     */
    public static String truncateForScoreboard(String text) {
        if (text == null) {
            return "";
        }
        // Minecraft scoreboard имеет ограничение ~32 символа на строку
        return Utils.truncate(text, 32);
    }
}