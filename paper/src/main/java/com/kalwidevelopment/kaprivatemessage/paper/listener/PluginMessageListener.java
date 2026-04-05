package com.kalwidevelopment.kaprivatemessage.paper.listener;

import com.google.gson.JsonObject;
import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import com.kalwidevelopment.kaprivatemessage.paper.gui.SoundSelectorGUI;
import com.kalwidevelopment.kaprivatemessage.paper.hook.EssentialsHook;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private final KAPrivateMessagePaper plugin;
    private final EssentialsHook essentialsHook;
    private final SoundSelectorGUI soundSelectorGUI;

    public PluginMessageListener(KAPrivateMessagePaper plugin, EssentialsHook essentialsHook, SoundSelectorGUI soundSelectorGUI) {
        this.plugin = plugin;
        this.essentialsHook = essentialsHook;
        this.soundSelectorGUI = soundSelectorGUI;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player msgPlayer, byte[] message) {
        if (!channel.equals(Constants.PLUGIN_CHANNEL)) return;

        try {
            JsonObject packet = PacketUtil.fromBytes(message);
            String type = packet.get("type").getAsString();

            switch (type) {
                case Constants.PACKET_PLAY_SOUND -> {
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    String soundId = packet.get("soundId").getAsString();
                    float volume = packet.get("volume").getAsFloat();
                    float pitch = packet.get("pitch").getAsFloat();

                    Player target = Bukkit.getPlayer(playerUUID);
                    if (target != null && target.isOnline()) {
                        try {
                            Sound sound = Sound.valueOf(soundId.toUpperCase().replace(".", "_").replace(":", "_"));
                            target.playSound(target.getLocation(), sound, volume, pitch);
                        } catch (IllegalArgumentException e) {
                            target.playSound(target.getLocation(), soundId, volume, pitch);
                        }
                    }
                }
                case Constants.PACKET_REQUEST_SOUND_GUI -> {
                    UUID playerUUID = UUID.fromString(packet.get("playerUUID").getAsString());
                    Player target = Bukkit.getPlayer(playerUUID);
                    if (target != null && target.isOnline()) {
                        Bukkit.getScheduler().runTask(plugin, () -> soundSelectorGUI.openGUI(target));
                    }
                }
                case Constants.PACKET_PM_SERVERS_RESPONSE -> {
                    UUID requesterUUID = UUID.fromString(packet.get("requesterUUID").getAsString());
                    String servers = packet.has("servers") ? packet.get("servers").getAsString() : "";
                    Player requester = Bukkit.getPlayer(requesterUUID);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage("Connected backend servers:");
                        if (servers.isEmpty()) {
                            requester.sendMessage("- (none)");
                        } else {
                            for (String entry : servers.split(",")) {
                                String[] parts = entry.split(":", 2);
                                String serverName = parts.length > 0 ? parts[0] : "unknown";
                                String playerCount = parts.length > 1 ? parts[1] : "0";
                                requester.sendMessage("- " + serverName + " (" + playerCount + " player)");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling plugin message: " + e.getMessage());
        }
    }
}
