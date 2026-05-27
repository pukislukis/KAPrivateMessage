package com.kalwidevelopment.kaprivatemessage.paper.hook;

import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final KAPrivateMessagePaper plugin;

    public PlaceholderAPIHook(KAPrivateMessagePaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "pm"; }

    @Override
    public @NotNull String getAuthor() { return "KalwiDevelopment"; }

    @Override
    public @NotNull String getVersion() { return "1.0.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String identifier) {
        return switch (identifier) {
            case "status" -> "Active";
            case "plugin_name" -> "KAPrivateMessage";
            case "plugin_version" -> "1.0.0";
            case "server_id" -> plugin.getPaperPluginConfig().getServerId();
            case "server_name" -> plugin.getPaperPluginConfig().getServerName();
            case "server_formatted" -> plugin.getPaperPluginConfig().getServerFormatted();
            default -> null;
        };
    }
}
