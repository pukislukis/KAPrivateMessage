package com.kalwidevelopment.kaprivatemessage.velocity.messaging;

import com.google.gson.JsonObject;
import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.common.PrivacyLevel;
import com.kalwidevelopment.kaprivatemessage.velocity.command.MessageCommand;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.DebugLogger;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.ServerConnection;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                        String realName = packet.has("realName") ? packet.get("realName").getAsString() : "";
                        String prefix = packet.has("prefix") ? packet.get("prefix").getAsString() : "";
                        String serverId = packet.has("serverId") ? packet.get("serverId").getAsString() : "";
                        String serverName = packet.has("serverName") ? packet.get("serverName").getAsString() : "";
                        String serverFormatted = packet.has("serverFormatted") ? packet.get("serverFormatted").getAsString() : "";
                        plugin.getPlayerDataManager().cachePlayerMeta(
                            playerUUID, nickname, realName, prefix, serverId, serverName, serverFormatted
                        );
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
                case Constants.PACKET_PM_SEND_REQUEST -> {
                    if (plugin == null) break;
                    UUID senderUUID = UUID.fromString(packet.get("senderUUID").getAsString());
                    String targetName = packet.has("targetName") ? packet.get("targetName").getAsString() : "";
                    String message = packet.has("message") ? packet.get("message").getAsString() : "";
                    plugin.getServer().getPlayer(senderUUID).ifPresentOrElse(sender ->
                        new MessageCommand(plugin).executeAs(sender, targetName, message, "paper-bridge"),
                        () -> DebugLogger.log(plugin, "PM bridge request ignored, sender offline uuid=" + senderUUID)
                    );
                }
                case Constants.PACKET_PM_SERVERS_REQUEST -> {
                    if (plugin == null) break;
                    UUID requesterUUID = UUID.fromString(packet.get("requesterUUID").getAsString());
                    List<RegisteredServer> servers = plugin.getServer().getAllServers().stream()
                        .sorted(Comparator.comparing(s -> s.getServerInfo().getName()))
                        .collect(Collectors.toList());
                    String serversPayload = servers.stream()
                        .map(s -> s.getServerInfo().getName() + ":" + s.getPlayersConnected().size())
                        .collect(Collectors.joining(","));
                    sendToServer(requesterUUID, PacketUtil.pmServersResponsePacket(requesterUUID.toString(), serversPayload));
                }
                case Constants.PACKET_PM_PRIVACY_SET -> {
                    if (plugin == null) break;
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    String level = packet.has("privacyLevel") ? packet.get("privacyLevel").getAsString() : "NONE";
                    plugin.getPlayerDataManager().setPrivacy(playerUUID, PrivacyLevel.fromString(level));
                }
                case Constants.PACKET_PM_IGNORES_LIST_REQUEST -> {
                    if (plugin == null) break;
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    String names = plugin.getPlayerDataManager().getIgnoreList(playerUUID).stream()
                        .map(uuid -> plugin.getServer().getPlayer(uuid).map(Player::getUsername).orElse(uuid.toString()))
                        .collect(Collectors.joining(","));
                    sendToServer(playerUUID, PacketUtil.pmIgnoresListResponsePacket(playerUUID.toString(), names));
                }
                case Constants.PACKET_PM_IGNORES_CLEAR -> {
                    if (plugin == null) break;
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    plugin.getPlayerDataManager().clearIgnore(playerUUID);
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

    public void logPrivateMessageCommand(Player sender, Player target, String message) {
        sendToServer(sender, PacketUtil.pmCommandLogPacket(
            sender.getUniqueId().toString(),
            sender.getUsername(),
            target.getUsername(),
            message
        ));
    }
}
