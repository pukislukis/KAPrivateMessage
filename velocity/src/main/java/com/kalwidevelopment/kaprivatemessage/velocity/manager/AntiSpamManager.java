package com.kalwidevelopment.kaprivatemessage.velocity.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamManager {
    private final int cooldownSeconds;
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    public AntiSpamManager(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public boolean isOnCooldown(UUID uuid) {
        Long last = lastMessageTime.get(uuid);
        if (last == null) return false;
        return System.currentTimeMillis() - last < cooldownSeconds * 1000L;
    }

    public long getRemainingSeconds(UUID uuid) {
        Long last = lastMessageTime.get(uuid);
        if (last == null) return 0;
        long elapsed = System.currentTimeMillis() - last;
        long remaining = cooldownSeconds * 1000L - elapsed;
        return remaining > 0 ? (remaining / 1000) + 1 : 0;
    }

    public void recordMessage(UUID uuid) {
        lastMessageTime.put(uuid, System.currentTimeMillis());
    }

    public void cleanup(UUID uuid) {
        lastMessageTime.remove(uuid);
    }
}
