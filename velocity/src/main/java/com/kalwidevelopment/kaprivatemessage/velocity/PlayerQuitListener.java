package com.kalwidevelopment.kaprivatemessage.velocity;

import com.kalwidevelopment.kaprivatemessage.velocity.manager.AntiSpamManager;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.PlayerDataManager;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.SocialSpyManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public class PlayerQuitListener {

    private final PlayerDataManager playerDataManager;
    private final SocialSpyManager socialSpyManager;
    private final AntiSpamManager antiSpamManager;

    public PlayerQuitListener(PlayerDataManager playerDataManager,
                               SocialSpyManager socialSpyManager,
                               AntiSpamManager antiSpamManager) {
        this.playerDataManager = playerDataManager;
        this.socialSpyManager = socialSpyManager;
        this.antiSpamManager = antiSpamManager;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        antiSpamManager.cleanup(uuid);
        socialSpyManager.remove(uuid);
    }
}
