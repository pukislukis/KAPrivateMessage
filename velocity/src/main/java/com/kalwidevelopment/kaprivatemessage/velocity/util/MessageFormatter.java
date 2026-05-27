package com.kalwidevelopment.kaprivatemessage.velocity.util;

import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Optional;

public class MessageFormatter {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static Component formatSendMessage(KAPrivateMessageVelocity plugin, Player sender, Player target, String message) {
        String template = plugin.getPluginConfig().getMessage("format-send");
        String result = PlaceholderEngine.applyMessagePlaceholders(plugin, template, sender, target, message);
        return MM.deserialize(result);
    }

    public static Component formatReceiveMessage(KAPrivateMessageVelocity plugin, Player sender, Player target, String message) {
        String template = plugin.getPluginConfig().getMessage("format-receive");
        String result = PlaceholderEngine.applyMessagePlaceholders(plugin, template, sender, target, message);
        return MM.deserialize(result);
    }

    public static Component formatSpyMessage(KAPrivateMessageVelocity plugin, Player sender, Player target, String message) {
        String template = plugin.getPluginConfig().getMessage("format-socialspy");
        String result = PlaceholderEngine.applyMessagePlaceholders(plugin, template, sender, target, message);
        return MM.deserialize(result);
    }

    public static String getDisplayName(KAPrivateMessageVelocity plugin, Player player) {
        Optional<String> nick = plugin.getPlayerDataManager().getCachedNickname(player.getUniqueId());
        return nick.orElse(player.getUsername());
    }

    public static Component parse(String message) {
        return MM.deserialize(message);
    }

    public static Component parse(String message, String... replacements) {
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return MM.deserialize(message);
    }
}
