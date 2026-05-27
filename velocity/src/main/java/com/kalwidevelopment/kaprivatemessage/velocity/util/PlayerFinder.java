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
        String normalizedQuery = normalizeName(query);
        if (!normalizedQuery.equalsIgnoreCase(query)) {
            Optional<Player> byNormalizedName = plugin.getServer().getPlayer(normalizedQuery);
            if (byNormalizedName.isPresent()) return byNormalizedName;
        }

        // 2. Exact nickname (case-insensitive) only for same-server targets
        String senderServer = sender == null ? "" : sender.getCurrentServer()
            .map(c -> c.getServerInfo().getName())
            .orElse("");
        Map<UUID, String> nicks = plugin.getPlayerDataManager().getAllNicknames();
        for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
            if (sender != null && entry.getKey().equals(sender.getUniqueId())) continue;
            if (nameEquals(entry.getValue(), query)) {
                Optional<Player> target = plugin.getServer().getPlayer(entry.getKey());
                if (target.isEmpty()) continue;
                if (sender == null || isSameServer(senderServer, target.get())) {
                    return target;
                }
            }
        }

        // 3. Partial nickname (min 3 chars)
        if (normalizedQuery.length() >= Constants.MIN_SEARCH_LENGTH) {
            String lowerQuery = normalizedQuery.toLowerCase();
            for (Map.Entry<UUID, String> entry : nicks.entrySet()) {
                if (sender != null && entry.getKey().equals(sender.getUniqueId())) continue;
                String normalizedNick = normalizeName(entry.getValue()).toLowerCase();
                if (normalizedNick.contains(lowerQuery)) {
                    Optional<Player> target = plugin.getServer().getPlayer(entry.getKey());
                    if (target.isEmpty()) continue;
                    if (sender == null || isSameServer(senderServer, target.get())) {
                        return target;
                    }
                }
            }
            // 4. Partial real name
            for (Player p : plugin.getServer().getAllPlayers()) {
                if (sender != null && p.getUniqueId().equals(sender.getUniqueId())) continue;
                String raw = p.getUsername().toLowerCase();
                String normalized = normalizeName(p.getUsername()).toLowerCase();
                if (raw.contains(query.toLowerCase()) || normalized.contains(lowerQuery)) {
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

    private static String normalizeName(String name) {
        if (name == null) return "";
        String out = name;
        if (out.startsWith(".")) out = out.substring(1);
        out = out.replaceAll("(?i)[&§]x([&§][0-9a-f]){6}", "");
        out = out.replaceAll("(?i)[&§]#[0-9a-f]{6}", "");
        out = out.replaceAll("(?i)[&§][0-9a-fk-or]", "");
        out = out.replaceAll("<[^>]+>", "");
        return out.trim();
    }

    private static boolean nameEquals(String left, String right) {
        if (left == null || right == null) return false;
        return left.equalsIgnoreCase(right) || normalizeName(left).equalsIgnoreCase(normalizeName(right));
    }
}
