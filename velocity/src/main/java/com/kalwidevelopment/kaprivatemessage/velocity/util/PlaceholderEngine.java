package com.kalwidevelopment.kaprivatemessage.velocity.util;

import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.PlayerDataManager;
import com.velocitypowered.api.proxy.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlaceholderEngine {
    private PlaceholderEngine() {}

    public static String applyMessagePlaceholders(KAPrivateMessageVelocity plugin, String template,
                                                  Player sender, Player receiver, String message) {
        Map<String, String> values = new LinkedHashMap<>();

        PlayerDataManager.PlayerMeta senderMeta = plugin.getPlayerDataManager().getPlayerMeta(sender.getUniqueId());
        PlayerDataManager.PlayerMeta receiverMeta = plugin.getPlayerDataManager().getPlayerMeta(receiver.getUniqueId());
        String senderPrefix = senderMeta.getPrefix();
        String receiverPrefix = receiverMeta.getPrefix();
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isEnabled()) {
            if (senderPrefix == null || senderPrefix.isEmpty()) {
                senderPrefix = plugin.getLuckPermsHook().getPrefix(sender);
            }
            if (receiverPrefix == null || receiverPrefix.isEmpty()) {
                receiverPrefix = plugin.getLuckPermsHook().getPrefix(receiver);
            }
        }

        String senderDisplay = MessageFormatter.getDisplayName(plugin, sender);
        String receiverDisplay = MessageFormatter.getDisplayName(plugin, receiver);

        put(values, "sender-name", senderDisplay);
        put(values, "receiver-name", receiverDisplay);
        put(values, "reciever-name", receiverDisplay);
        put(values, "sender-realname", senderMeta.getRealName().isEmpty() ? sender.getUsername() : senderMeta.getRealName());
        put(values, "receiver-realname", receiverMeta.getRealName().isEmpty() ? receiver.getUsername() : receiverMeta.getRealName());
        put(values, "reciever-realname", receiverMeta.getRealName().isEmpty() ? receiver.getUsername() : receiverMeta.getRealName());
        put(values, "sender-prefix", senderPrefix);
        put(values, "receiver-prefix", receiverPrefix);
        put(values, "reciever-prefix", receiverPrefix);
        put(values, "sender-server-id", senderMeta.getServerId());
        put(values, "sender-server-name", senderMeta.getServerName());
        put(values, "sender-server-formatted", senderMeta.getServerFormatted());
        put(values, "receiver-server-id", receiverMeta.getServerId());
        put(values, "receiver-server-name", receiverMeta.getServerName());
        put(values, "receiver-server-formatted", receiverMeta.getServerFormatted());
        put(values, "reciever-server-id", receiverMeta.getServerId());
        put(values, "reciever-server-name", receiverMeta.getServerName());
        put(values, "reciever-server-formatted", receiverMeta.getServerFormatted());
        put(values, "message", message);

        put(values, "sender", senderDisplay);
        put(values, "target", receiverDisplay);

        String out = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            out = out.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return out;
    }

    private static void put(Map<String, String> values, String key, String value) {
        values.put(key, value == null ? "" : value);
    }
}
