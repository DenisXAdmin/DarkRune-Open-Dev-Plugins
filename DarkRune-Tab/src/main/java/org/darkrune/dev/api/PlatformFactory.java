package org.darkrune.dev.api;

import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.platform.FoliaAdapter;
import org.darkrune.dev.platform.PaperAdapter;

/**
 * Фабрика адаптеров.
 * Создаёт адаптер в зависимости от обнаруженной платформы.
 */
public final class PlatformFactory {

    private PlatformFactory() {
    }

    public static PlatformAdapter createAdapter(DarkRuneTab plugin) {
        PlatformDetector.Platform platform = PlatformDetector.detect();

        return switch (platform) {
            case FOLIA -> new FoliaAdapter(plugin);
            case PAPER, PURPUR, UNKNOWN -> new PaperAdapter(plugin);
        };
    }
}