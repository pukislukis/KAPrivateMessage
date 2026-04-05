package com.kalwidevelopment.kaprivatemessage.velocity.hook;

import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;

public class LuckPermsHook {

    private final Logger logger;
    private LuckPerms luckPerms;
    private boolean enabled = false;

    public LuckPermsHook(Logger logger) {
        this.logger = logger;
    }

    public void setup() {
        try {
            this.luckPerms = LuckPermsProvider.get();
            this.enabled = true;
            logger.info("LuckPerms hook enabled.");
        } catch (Exception e) {
            logger.info("LuckPerms not found. Prefix fallback disabled.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPrefix(Player player) {
        if (!enabled || luckPerms == null || player == null) return "";
        try {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            if (user == null) return "";
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix == null ? "" : prefix;
        } catch (Exception e) {
            return "";
        }
    }
}
