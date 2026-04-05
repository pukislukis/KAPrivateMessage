package com.kalwidevelopment.kaprivatemessage.velocity.manager;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PrivacyLevel;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Path dataDirectory;
    private final Logger logger;

    private final Map<UUID, PrivacyLevel> privacyMap = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> replyMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> nicknameCache = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> soundToggle = new ConcurrentHashMap<>();
    private final Map<UUID, String> soundIdSend = new ConcurrentHashMap<>();
    private final Map<UUID, String> soundIdReceive = new ConcurrentHashMap<>();
    private final Map<UUID, Double> soundVolSend = new ConcurrentHashMap<>();
    private final Map<UUID, Double> soundVolReceive = new ConcurrentHashMap<>();
    private final Map<UUID, Double> soundPitchSend = new ConcurrentHashMap<>();
    private final Map<UUID, Double> soundPitchReceive = new ConcurrentHashMap<>();

    public PlayerDataManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        Path dataFile = dataDirectory.resolve("playerdata.yml");
        if (!Files.exists(dataFile)) return;
        try (Reader reader = Files.newBufferedReader(dataFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            if (data == null) return;

            Map<String, String> privacy = (Map<String, String>) data.get("privacy");
            if (privacy != null) {
                privacy.forEach((k, v) -> privacyMap.put(UUID.fromString(k), PrivacyLevel.fromString(v)));
            }
            Map<String, List<String>> ignore = (Map<String, List<String>>) data.get("ignore");
            if (ignore != null) {
                ignore.forEach((k, v) -> {
                    Set<UUID> set = new HashSet<>();
                    v.forEach(s -> set.add(UUID.fromString(s)));
                    ignoreMap.put(UUID.fromString(k), set);
                });
            }
            Map<String, Boolean> sounds = (Map<String, Boolean>) data.get("sound-toggle");
            if (sounds != null) {
                sounds.forEach((k, v) -> soundToggle.put(UUID.fromString(k), v));
            }
            Map<String, String> sSend = (Map<String, String>) data.get("sound-id-send");
            if (sSend != null) sSend.forEach((k, v) -> soundIdSend.put(UUID.fromString(k), v));
            Map<String, String> sReceive = (Map<String, String>) data.get("sound-id-receive");
            if (sReceive != null) sReceive.forEach((k, v) -> soundIdReceive.put(UUID.fromString(k), v));
            Map<String, Double> vSend = (Map<String, Double>) data.get("sound-vol-send");
            if (vSend != null) vSend.forEach((k, v) -> soundVolSend.put(UUID.fromString(k), v));
            Map<String, Double> vReceive = (Map<String, Double>) data.get("sound-vol-receive");
            if (vReceive != null) vReceive.forEach((k, v) -> soundVolReceive.put(UUID.fromString(k), v));
            Map<String, Double> pSend = (Map<String, Double>) data.get("sound-pitch-send");
            if (pSend != null) pSend.forEach((k, v) -> soundPitchSend.put(UUID.fromString(k), v));
            Map<String, Double> pReceive = (Map<String, Double>) data.get("sound-pitch-receive");
            if (pReceive != null) pReceive.forEach((k, v) -> soundPitchReceive.put(UUID.fromString(k), v));
        } catch (Exception e) {
            logger.error("Failed to load playerdata.yml", e);
        }
    }

    public void save() {
        try {
            if (!Files.exists(dataDirectory)) Files.createDirectories(dataDirectory);
            Path dataFile = dataDirectory.resolve("playerdata.yml");
            Map<String, Object> data = new LinkedHashMap<>();

            Map<String, String> privacy = new LinkedHashMap<>();
            privacyMap.forEach((k, v) -> privacy.put(k.toString(), v.name()));
            data.put("privacy", privacy);

            Map<String, List<String>> ignore = new LinkedHashMap<>();
            ignoreMap.forEach((k, v) -> {
                List<String> list = new ArrayList<>();
                v.forEach(u -> list.add(u.toString()));
                ignore.put(k.toString(), list);
            });
            data.put("ignore", ignore);

            Map<String, Boolean> sounds = new LinkedHashMap<>();
            soundToggle.forEach((k, v) -> sounds.put(k.toString(), v));
            data.put("sound-toggle", sounds);

            Map<String, String> sSend = new LinkedHashMap<>();
            soundIdSend.forEach((k, v) -> sSend.put(k.toString(), v));
            data.put("sound-id-send", sSend);

            Map<String, String> sReceive = new LinkedHashMap<>();
            soundIdReceive.forEach((k, v) -> sReceive.put(k.toString(), v));
            data.put("sound-id-receive", sReceive);

            Map<String, Double> vSend = new LinkedHashMap<>();
            soundVolSend.forEach((k, v) -> vSend.put(k.toString(), v));
            data.put("sound-vol-send", vSend);

            Map<String, Double> vReceive = new LinkedHashMap<>();
            soundVolReceive.forEach((k, v) -> vReceive.put(k.toString(), v));
            data.put("sound-vol-receive", vReceive);

            Map<String, Double> pSend = new LinkedHashMap<>();
            soundPitchSend.forEach((k, v) -> pSend.put(k.toString(), v));
            data.put("sound-pitch-send", pSend);

            Map<String, Double> pReceive = new LinkedHashMap<>();
            soundPitchReceive.forEach((k, v) -> pReceive.put(k.toString(), v));
            data.put("sound-pitch-receive", pReceive);

            Yaml yaml = new Yaml();
            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                yaml.dump(data, writer);
            }
        } catch (IOException e) {
            logger.error("Failed to save playerdata.yml", e);
        }
    }

    public PrivacyLevel getPrivacy(UUID uuid) {
        return privacyMap.getOrDefault(uuid, PrivacyLevel.NONE);
    }
    public void setPrivacy(UUID uuid, PrivacyLevel level) {
        privacyMap.put(uuid, level);
        save();
    }

    public Set<UUID> getIgnoreList(UUID uuid) {
        return ignoreMap.computeIfAbsent(uuid, k -> new HashSet<>());
    }
    public void addIgnore(UUID player, UUID ignored) {
        getIgnoreList(player).add(ignored);
        save();
    }
    public void removeIgnore(UUID player, UUID ignored) {
        getIgnoreList(player).remove(ignored);
        save();
    }
    public void clearIgnore(UUID player) {
        ignoreMap.remove(player);
        save();
    }
    public boolean isIgnoring(UUID player, UUID target) {
        return getIgnoreList(player).contains(target);
    }

    public Optional<UUID> getReplyTarget(UUID player) {
        return Optional.ofNullable(replyMap.get(player));
    }
    public void setReplyTarget(UUID player, UUID target) {
        replyMap.put(player, target);
        replyMap.put(target, player);
    }

    public void cacheNickname(UUID uuid, String nickname) {
        if (nickname == null || nickname.isEmpty() || nickname.equals("<none>")) {
            nicknameCache.remove(uuid);
        } else {
            nicknameCache.put(uuid, nickname);
        }
    }
    public Optional<String> getCachedNickname(UUID uuid) {
        return Optional.ofNullable(nicknameCache.get(uuid));
    }
    public Map<UUID, String> getAllNicknames() {
        return Collections.unmodifiableMap(nicknameCache);
    }

    public boolean isSoundEnabled(UUID uuid) {
        return soundToggle.getOrDefault(uuid, true);
    }
    public void setSoundEnabled(UUID uuid, boolean enabled) {
        if (enabled) soundToggle.remove(uuid);
        else soundToggle.put(uuid, false);
        save();
    }
    public String getSoundId(UUID uuid, String type) {
        if ("send".equals(type)) return soundIdSend.getOrDefault(uuid, Constants.DEFAULT_SOUND_SEND);
        return soundIdReceive.getOrDefault(uuid, Constants.DEFAULT_SOUND_RECEIVE);
    }
    public double getSoundVolume(UUID uuid, String type) {
        if ("send".equals(type)) return soundVolSend.getOrDefault(uuid, Constants.DEFAULT_VOLUME);
        return soundVolReceive.getOrDefault(uuid, Constants.DEFAULT_VOLUME);
    }
    public double getSoundPitch(UUID uuid, String type) {
        if ("send".equals(type)) return soundPitchSend.getOrDefault(uuid, Constants.DEFAULT_PITCH);
        return soundPitchReceive.getOrDefault(uuid, Constants.DEFAULT_PITCH);
    }
    public void setSoundSettings(UUID uuid, String type, String soundId, double volume, double pitch) {
        if ("send".equals(type)) {
            soundIdSend.put(uuid, soundId);
            soundVolSend.put(uuid, volume);
            soundPitchSend.put(uuid, pitch);
        } else {
            soundIdReceive.put(uuid, soundId);
            soundVolReceive.put(uuid, volume);
            soundPitchReceive.put(uuid, pitch);
        }
        save();
    }
}
