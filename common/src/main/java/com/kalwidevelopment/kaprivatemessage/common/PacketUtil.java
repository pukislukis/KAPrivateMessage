package com.kalwidevelopment.kaprivatemessage.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;

public final class PacketUtil {
    private PacketUtil() {}

    public static byte[] toBytes(JsonObject json) {
        String s = json.toString();
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static JsonObject fromBytes(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        return JsonParser.parseString(s).getAsJsonObject();
    }

    public static JsonObject playSoundPacket(String playerUUID, String soundId, double volume, double pitch, String soundType) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PLAY_SOUND);
        obj.addProperty("playerUUID", playerUUID);
        obj.addProperty("soundId", soundId);
        obj.addProperty("volume", volume);
        obj.addProperty("pitch", pitch);
        obj.addProperty("soundType", soundType);
        return obj;
    }

    public static JsonObject requestSoundGuiPacket(String playerUUID) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_REQUEST_SOUND_GUI);
        obj.addProperty("playerUUID", playerUUID);
        return obj;
    }

    public static JsonObject requestSettingsGuiPacket(String playerUUID) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_REQUEST_SETTINGS_GUI);
        obj.addProperty("playerUUID", playerUUID);
        return obj;
    }

    public static JsonObject nicknameUpdatePacket(String playerUUID, String nickname) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_NICKNAME_UPDATE);
        obj.addProperty("playerUUID", playerUUID);
        obj.addProperty("nickname", nickname);
        return obj;
    }

    public static JsonObject nicknameUpdatePacket(String playerUUID, String nickname, String realName, String prefix,
                                                  String serverId, String serverName, String serverFormatted) {
        JsonObject obj = nicknameUpdatePacket(playerUUID, nickname);
        obj.addProperty("realName", realName);
        obj.addProperty("prefix", prefix);
        obj.addProperty("serverId", serverId);
        obj.addProperty("serverName", serverName);
        obj.addProperty("serverFormatted", serverFormatted);
        return obj;
    }

    public static JsonObject soundSelectedPacket(String playerUUID, String soundType, String soundId, double volume, double pitch) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_SOUND_SELECTED);
        obj.addProperty("playerUUID", playerUUID);
        obj.addProperty("soundType", soundType);
        obj.addProperty("soundId", soundId);
        obj.addProperty("volume", volume);
        obj.addProperty("pitch", pitch);
        return obj;
    }

    public static JsonObject pmSendRequestPacket(String senderUUID, String targetName, String message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_SEND_REQUEST);
        obj.addProperty("senderUUID", senderUUID);
        obj.addProperty("targetName", targetName);
        obj.addProperty("message", message);
        return obj;
    }

    public static JsonObject pmServersRequestPacket(String requesterUUID) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_SERVERS_REQUEST);
        obj.addProperty("requesterUUID", requesterUUID);
        return obj;
    }

    public static JsonObject pmServersResponsePacket(String requesterUUID, String servers) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_SERVERS_RESPONSE);
        obj.addProperty("requesterUUID", requesterUUID);
        obj.addProperty("servers", servers);
        return obj;
    }

    public static JsonObject pmPrivacySetPacket(String playerUUID, String privacyLevel) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_PRIVACY_SET);
        obj.addProperty("playerUUID", playerUUID);
        obj.addProperty("privacyLevel", privacyLevel);
        return obj;
    }

    public static JsonObject pmIgnoresListRequestPacket(String playerUUID) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_IGNORES_LIST_REQUEST);
        obj.addProperty("playerUUID", playerUUID);
        return obj;
    }

    public static JsonObject pmIgnoresClearPacket(String playerUUID) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_IGNORES_CLEAR);
        obj.addProperty("playerUUID", playerUUID);
        return obj;
    }

    public static JsonObject pmIgnoresListResponsePacket(String playerUUID, String players) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_IGNORES_LIST_RESPONSE);
        obj.addProperty("playerUUID", playerUUID);
        obj.addProperty("players", players);
        return obj;
    }

    public static JsonObject pmCommandLogPacket(String senderName, String targetName, String message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_PM_COMMAND_LOG);
        obj.addProperty("senderName", senderName);
        obj.addProperty("targetName", targetName);
        obj.addProperty("message", message);
        return obj;
    }
}
