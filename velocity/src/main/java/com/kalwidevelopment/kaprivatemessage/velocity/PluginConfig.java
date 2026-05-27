package com.kalwidevelopment.kaprivatemessage.velocity;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class PluginConfig {

    private final Path dataDirectory;
    private final Logger logger;

    private int cooldownSeconds = 3;
    private String defaultSoundSend = "block.note_block.hat";
    private String defaultSoundReceive = "entity.experience_orb.pickup";
    private double defaultVolume = 1.0;
    private double defaultPitch = 1.0;
    private boolean debugEnabled = false;

    private Map<String, Object> messages;

    public PluginConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            Path configFile = dataDirectory.resolve("config.yml");
            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    }
                }
            }
            Yaml yaml = new Yaml();
            Map<String, Object> raw;
            try (Reader reader = Files.newBufferedReader(configFile)) {
                raw = yaml.load(reader);
            }
            if (raw != null) {
                Object cd = raw.get("cooldown-seconds");
                if (cd instanceof Number) cooldownSeconds = ((Number) cd).intValue();
                Object debug = raw.get("debug");
                if (debug instanceof Boolean) debugEnabled = (Boolean) debug;
                Map<String, Object> sounds = (Map<String, Object>) raw.get("sounds");
                if (sounds != null) {
                    Object ds = sounds.get("default-send");
                    if (ds != null) defaultSoundSend = ds.toString();
                    Object dr = sounds.get("default-receive");
                    if (dr != null) defaultSoundReceive = dr.toString();
                    Object vol = sounds.get("default-volume");
                    if (vol instanceof Number) defaultVolume = ((Number) vol).doubleValue();
                    Object pitch = sounds.get("default-pitch");
                    if (pitch instanceof Number) defaultPitch = ((Number) pitch).doubleValue();
                }
                messages = (Map<String, Object>) raw.get("messages");
            }
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    public String getMessage(String key) {
        if (messages == null) return key;
        Object val = messages.get(key);
        return val != null ? val.toString() : key;
    }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public String getDefaultSoundSend() { return defaultSoundSend; }
    public String getDefaultSoundReceive() { return defaultSoundReceive; }
    public double getDefaultVolume() { return defaultVolume; }
    public double getDefaultPitch() { return defaultPitch; }
    public boolean isDebugEnabled() { return debugEnabled; }
}
