package com.kalwidevelopment.kaprivatemessage.velocity.messaging;

import com.google.gson.JsonObject;
import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import org.slf4j.Logger;

import java.util.UUID;

public class VelocityMessageBridge {

    private final ProxyServer server;
    private final Logger logger;
    private KAPrivateMessageVelocity plugin;

    public VelocityMessageBridge(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public void setPlugin(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(KAPrivateMessageVelocity.CHANNEL)) return;
        if (!(event.getSource() instanceof ServerConnection)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        try {
            JsonObject packet = PacketUtil.fromBytes(event.getData());
            String type = packet.get("type").getAsString();

            switch (type) {
                case Constants.PACKET_NICKNAME_UPDATE -> {
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    String nickname = packet.has("nickname") ? packet.get("nickname").getAsString() : null;
                    if (plugin != null) {
                        plugin.getPlayerDataManager().cacheNickname(playerUUID, nickname);
                    }
                }
                case Constants.PACKET_SOUND_SELECTED -> {
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    String soundType = packet.get("soundType").getAsString();
                    String soundId = packet.get("soundId").getAsString();
                    double volume = packet.get("volume").getAsDouble();
                    double pitch = packet.get("pitch").getAsDouble();
                    if (plugin != null) {
                        plugin.getPlayerDataManager().setSoundSettings(playerUUID, soundType, soundId, volume, pitch);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error handling plugin message", e);
        }
    }

    public void sendToServer(Player player, JsonObject packet) {
        player.getCurrentServer().ifPresent(serverConn ->
            serverConn.sendPluginMessage(KAPrivateMessageVelocity.CHANNEL, PacketUtil.toBytes(packet))
        );
    }

    public void sendToServer(UUID playerUUID, JsonObject packet) {
        server.getPlayer(playerUUID).ifPresent(player -> sendToServer(player, packet));
    }

    public void playSound(Player player, String soundType) {
        if (plugin == null) return;
        UUID uuid = player.getUniqueId();
        if (!plugin.getPlayerDataManager().isSoundEnabled(uuid)) return;
        String soundId = plugin.getPlayerDataManager().getSoundId(uuid, soundType);
        double volume = plugin.getPlayerDataManager().getSoundVolume(uuid, soundType);
        double pitch = plugin.getPlayerDataManager().getSoundPitch(uuid, soundType);
        sendToServer(player, PacketUtil.playSoundPacket(uuid.toString(), soundId, volume, pitch, soundType));
    }
}
