package org.darkrune.dev.api;

/**
 * Детектор платформы (ядра сервера).
 * Определяет, на каком ядре запущен плагин: Paper, Folia или Purpur.
 * Результат кэшируется при первом вызове.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public final class PlatformDetector {

    /**
     * Поддерживаемые платформы.
     */
    public enum Platform {
        PAPER("Paper"),
        FOLIA("Folia"),
        PURPUR("Purpur"),
        UNKNOWN("Unknown");

        private final String displayName;

        Platform(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static volatile Platform cachedPlatform;

    private PlatformDetector() {
        // Utility class
    }

    /**
     * Определяет платформу сервера.
     * Результат кэшируется после первого вызова.
     *
     * @return определённая платформа
     */
    public static Platform detect() {
        Platform result = cachedPlatform;
        if (result != null) {
            return result;
        }

        synchronized (PlatformDetector.class) {
            if (cachedPlatform == null) {
                cachedPlatform = doDetect();
            }
            return cachedPlatform;
        }
    }

    /**
     * Внутренняя логика определения платформы.
     * Проверяет наличие специфичных классов каждого ядра.
     */
    private static Platform doDetect() {
        // Folia - самый специфичный, проверяем первым
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
            return Platform.FOLIA;
        }

        // Purpur - наследует Paper, но имеет свои конфиги
        if (classExists("org.purpurmc.purpur.PurpurConfig")) {
            return Platform.PURPUR;
        }

        // Paper
        if (classExists("com.destroystokyo.paper.PaperConfig")) {
            return Platform.PAPER;
        }

        return Platform.UNKNOWN;
    }

    /**
     * Проверяет наличие класса в classpath.
     */
    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Краткая проверка: работает ли на Folia.
     */
    public static boolean isFolia() {
        return detect() == Platform.FOLIA;
    }

    /**
     * Проверка: работает ли на семействе Paper (Paper, Folia, Purpur).
     */
    public static boolean isPaperFamily() {
        Platform p = detect();
        return p == Platform.PAPER || p == Platform.FOLIA || p == Platform.PURPUR;
    }

    /**
     * Сбрасывает кэш (для тестирования).
     */
    public static synchronized void reset() {
        cachedPlatform = null;
    }
}