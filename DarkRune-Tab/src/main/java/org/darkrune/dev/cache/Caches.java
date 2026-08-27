package org.darkrune.dev.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Все кэши плагина.
 */
public final class Caches {

    private Caches() {
    }

    public static final Cache<String, Component> COMPONENT_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofSeconds(1))
            .recordStats()
            .build();

    public static final Cache<UUID, Object> METADATA_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofSeconds(30))
            .recordStats()
            .build();

    public static final Cache<String, String> PLACEHOLDER_CACHE = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(Duration.ofSeconds(10))
            .recordStats()
            .build();

    public static void clearAll() {
        COMPONENT_CACHE.invalidateAll();
        METADATA_CACHE.invalidateAll();
        PLACEHOLDER_CACHE.invalidateAll();
    }

    public static void clearPlayer(UUID uuid) {
        METADATA_CACHE.invalidate(uuid);
    }

    public static String getStats() {
        return String.format(
                "Components: %d | MetaData: %d | Placeholders: %d",
                COMPONENT_CACHE.estimatedSize(),
                METADATA_CACHE.estimatedSize(),
                PLACEHOLDER_CACHE.estimatedSize()
        );
    }
}