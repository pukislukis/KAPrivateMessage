package com.kalwidevelopment.kaprivatemessage.paper.hook;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsHook {

    private final JavaPlugin plugin;
    private Essentials essentials;
    private boolean enabled = false;

    public EssentialsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        Plugin essPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (essPlugin instanceof Essentials) {
            this.essentials = (Essentials) essPlugin;
            this.enabled = true;
            plugin.getLogger().info("EssentialsX hook enabled.");
        } else {
            plugin.getLogger().info("EssentialsX not found. Nickname support disabled.");
        }
    }

    public boolean isEnabled() { return enabled; }

    public String getStrippedNickname(Player player) {
        if (!enabled || essentials == null) return null;
        try {
            User user = essentials.getUser(player);
            if (user == null) return null;
            String nick = user.getNickname();
            if (nick == null || nick.isEmpty()) return null;
            // Strip legacy Minecraft color codes (§0-9, §a-f, §k-o, §r, §x) and MiniMessage tags
            return nick.replaceAll("§[0-9a-fk-orx]", "").replaceAll("<[^>]+>", "").trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String getColoredNickname(Player player) {
        if (!enabled || essentials == null) return null;
        try {
            User user = essentials.getUser(player);
            if (user == null) return null;
            return user.getNickname();
        } catch (Exception e) {
            return null;
        }
    }
}
