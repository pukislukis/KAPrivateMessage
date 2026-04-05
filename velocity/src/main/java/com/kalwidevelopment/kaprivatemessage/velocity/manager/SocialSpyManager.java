package com.kalwidevelopment.kaprivatemessage.velocity.manager;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SocialSpyManager {
    private final Set<UUID> spies = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void add(UUID uuid) { spies.add(uuid); }
    public void remove(UUID uuid) { spies.remove(uuid); }
    public boolean toggle(UUID uuid) {
        if (spies.contains(uuid)) { spies.remove(uuid); return false; }
        else { spies.add(uuid); return true; }
    }
    public boolean isSpy(UUID uuid) { return spies.contains(uuid); }
    public Set<UUID> getSpies() { return Collections.unmodifiableSet(spies); }
}
