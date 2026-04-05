package com.kalwidevelopment.kaprivatemessage.paper.listener;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import com.kalwidevelopment.kaprivatemessage.paper.hook.EssentialsHook;
import com.kalwidevelopment.kaprivatemessage.paper.hook.LuckPermsHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerNicknameListener implements Listener {

    private final KAPrivateMessagePaper plugin;
    private final EssentialsHook essentialsHook;
    private final LuckPermsHook luckPermsHook;

    public PlayerNicknameListener(KAPrivateMessagePaper plugin, EssentialsHook essentialsHook, LuckPermsHook luckPermsHook) {
        this.plugin = plugin;
        this.essentialsHook = essentialsHook;
        this.luckPermsHook = luckPermsHook;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> sendNicknameUpdate(player), 20L);
    }

    public void sendNicknameUpdate(Player player) {
        String nickname = essentialsHook.getStrippedNickname(player);
        if (!plugin.getPaperPluginConfig().isSendNicknameInBridge()) {
            nickname = "";
        }
        String prefix = "";
        if (plugin.getPaperPluginConfig().isSendPrefixInBridge()) {
            prefix = luckPermsHook.getPrefix(player);
            if (prefix == null || prefix.isEmpty()) {
                prefix = essentialsHook.getPrefix(player);
            }
        }
        byte[] data = PacketUtil.toBytes(PacketUtil.nicknameUpdatePacket(
            player.getUniqueId().toString(),
            nickname != null ? nickname : "",
            player.getName(),
            prefix,
            plugin.getPaperPluginConfig().getServerId(),
            plugin.getPaperPluginConfig().getServerName(),
            plugin.getPaperPluginConfig().getServerFormatted()
        ));
        player.sendPluginMessage(plugin, Constants.PLUGIN_CHANNEL, data);
    }
}
