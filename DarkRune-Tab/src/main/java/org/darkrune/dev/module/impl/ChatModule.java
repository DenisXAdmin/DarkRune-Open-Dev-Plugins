package org.darkrune.dev.module.impl;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.darkrune.dev.DarkRuneTab;
import org.darkrune.dev.config.ChatConfig;
import org.darkrune.dev.module.DisplayModule;
import org.darkrune.dev.util.Utils;

/**
 * Модуль форматирования чата.
 *
 * @author DarkRune Dev
 * @since 1.0.0
 */
public class ChatModule implements DisplayModule, Listener {

    private final DarkRuneTab plugin;
    private volatile boolean enabled;

    public ChatModule(DarkRuneTab plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfigs().getChat().isEnabled();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override public String getName() { return "chat"; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public void initialize() {
        if (enabled) {
            plugin.getLogger().info("Chat module initialized");
        }
    }

    @Override
    public void reload() {
        this.enabled = plugin.getConfigs().getChat().isEnabled();
    }

    @Override
    public void shutdown() {}

    @Override
    public void resetAll() {
        // Чат не оставляет постоянных эффектов на клиентах
    }

    @Override
    public void update(Player player) {}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!enabled) {
            return;
        }

        Player player = event.getPlayer();
        ChatConfig chatConfig = plugin.getConfigs().getChat();

        String format = chatConfig.getFormatFor(
                perm -> plugin.getLuckPerms().hasPermission(player.getUniqueId(), perm));

        String resolved = plugin.getPlaceholderResolver().resolve(player, format);

        boolean canColor = !chatConfig.isStripPlayerColors()
                || plugin.getLuckPerms().hasPermission(player.getUniqueId(), chatConfig.getColorPermission());

        event.renderer((source, sourceDisplayName, message, audience) ->
                buildLine(resolved, message, canColor));
    }

    private Component buildLine(String resolvedFormat, Component message, boolean canColor) {
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(message);
        Component messageComponent = canColor
                ? Utils.parse(rawMessage)
                : Component.text(rawMessage);

        String[] parts = resolvedFormat.split("%message%", -1);
        Component result = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            result = result.append(Utils.parse(parts[i]));
            if (i < parts.length - 1) {
                result = result.append(messageComponent);
            }
        }
        return result;
    }
}