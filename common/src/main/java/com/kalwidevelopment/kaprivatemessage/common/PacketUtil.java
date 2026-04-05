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

    public static JsonObject nicknameUpdatePacket(String playerUUID, String nickname) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", Constants.PACKET_NICKNAME_UPDATE);
        obj.addProperty("playerUUID", playerUUID);
        obj.addProperty("nickname", nickname);
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
}
