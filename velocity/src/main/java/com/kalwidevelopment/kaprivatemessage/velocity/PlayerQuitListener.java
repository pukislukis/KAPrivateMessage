package com.kalwidevelopment.kaprivatemessage.velocity;

import com.kalwidevelopment.kaprivatemessage.velocity.manager.AntiSpamManager;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.PlayerDataManager;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.SocialSpyManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

import java.util.function.Supplier;

public class PlayerQuitListener {

    private final PlayerDataManager playerDataManager;
    private final SocialSpyManager socialSpyManager;
    private final Supplier<AntiSpamManager> antiSpamManagerSupplier;

    public PlayerQuitListener(PlayerDataManager playerDataManager,
                               SocialSpyManager socialSpyManager,
                               Supplier<AntiSpamManager> antiSpamManagerSupplier) {
        this.playerDataManager = playerDataManager;
        this.socialSpyManager = socialSpyManager;
        this.antiSpamManagerSupplier = antiSpamManagerSupplier;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        AntiSpamManager antiSpamManager = antiSpamManagerSupplier.get();
        if (antiSpamManager != null) {
            antiSpamManager.cleanup(uuid);
        }
        socialSpyManager.remove(uuid);
        // Clear nickname cache on the proxy when player disconnects
        playerDataManager.cacheNickname(uuid, null);
    }
}
