package com.kalwidevelopment.kaprivatemessage.paper;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperPluginConfig {
    private final JavaPlugin plugin;

    private String serverId = "lobby-1";
    private String serverName = "Lobby";
    private String serverFormatted = "<gray>[Lobby]</gray>";
    private boolean sendPrefixInBridge = true;
    private boolean sendNicknameInBridge = true;
    private String databaseType = "none";
    private String databaseHost = "127.0.0.1";
    private int databasePort = 3306;
    private String databaseName = "kaprivatemessage";
    private String databaseUsername = "root";
    private String databasePassword = "change_me";

    public PaperPluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        serverId = plugin.getConfig().getString("server.id", serverId);
        serverName = plugin.getConfig().getString("server.name", serverName);
        serverFormatted = plugin.getConfig().getString("server.formatted", serverFormatted);
        sendPrefixInBridge = plugin.getConfig().getBoolean("bridge.send-prefix", sendPrefixInBridge);
        sendNicknameInBridge = plugin.getConfig().getBoolean("bridge.send-nickname", sendNicknameInBridge);

        databaseType = plugin.getConfig().getString("database.type", databaseType);
        databaseHost = plugin.getConfig().getString("database.host", databaseHost);
        databasePort = plugin.getConfig().getInt("database.port", databasePort);
        databaseName = plugin.getConfig().getString("database.name", databaseName);
        databaseUsername = plugin.getConfig().getString("database.username", databaseUsername);
        databasePassword = plugin.getConfig().getString("database.password", databasePassword);
    }

    public String getServerId() { return serverId; }
    public String getServerName() { return serverName; }
    public String getServerFormatted() { return serverFormatted; }
    public boolean isSendPrefixInBridge() { return sendPrefixInBridge; }
    public boolean isSendNicknameInBridge() { return sendNicknameInBridge; }
    public String getDatabaseType() { return databaseType; }
    public String getDatabaseHost() { return databaseHost; }
    public int getDatabasePort() { return databasePort; }
    public String getDatabaseName() { return databaseName; }
    public String getDatabaseUsername() { return databaseUsername; }
    public String getDatabasePassword() { return databasePassword; }
}

