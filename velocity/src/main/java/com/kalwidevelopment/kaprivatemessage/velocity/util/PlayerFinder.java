package com.kalwidevelopment.kaprivatemessage.velocity.util;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.velocitypowered.api.proxy.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerFinder {

    public static Optional<Player> find(KAPrivateMessageVelocity plugin, String query) {
        return find(plugin, query, null);
    }

    public static Optional<Player> find(KAPrivateMessageVelocity plugin, String query, Player sender) {
        // 1. Exact real name
        Optional<Player> byName = plugin.getServer().getPlayer(query);
        if (byName.isPresent()) return byName;

        // 2. Exact nickname (case-insensitive) only for same-server targets
        String senderServer = sender == null ? "" : sender.getCurrentServer()
            .map(c -> c.getServerInfo().getName())
            .orElse("");
        Map<UUID, String> nicks = plugin.getPlayerDataManager().getAllNicknames();
        for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(query)) {
                Optional<Player> target = plugin.getServer().getPlayer(entry.getKey());
                if (target.isEmpty()) continue;
                if (sender == null || isSameServer(senderServer, target.get())) {
                    return target;
                }
            }
        }

        // 3. Partial nickname (min 3 chars)
        if (query.length() >= Constants.MIN_SEARCH_LENGTH) {
            String lowerQuery = query.toLowerCase();
            for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
                if (entry.getValue().toLowerCase().contains(lowerQuery)) {
                    Optional<Player> target = plugin.getServer().getPlayer(entry.getKey());
                    if (target.isEmpty()) continue;
                    if (sender == null || isSameServer(senderServer, target.get())) {
                        return target;
                    }
                }
            }
            // 4. Partial real name
            for (Player p : plugin.getServer().getAllPlayers()) {
                if (p.getUsername().toLowerCase().contains(lowerQuery)) {
                    return Optional.of(p);
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isSameServer(String senderServer, Player target) {
        if (senderServer.isEmpty()) return true;
        String targetServer = target.getCurrentServer()
            .map(c -> c.getServerInfo().getName())
            .orElse("");
        return senderServer.equalsIgnoreCase(targetServer);
    }
}
