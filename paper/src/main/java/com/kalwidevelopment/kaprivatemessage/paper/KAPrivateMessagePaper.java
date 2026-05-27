package com.kalwidevelopment.kaprivatemessage.paper;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.paper.command.PaperMessageCommand;
import com.kalwidevelopment.kaprivatemessage.paper.command.PaperReloadCommand;
import com.kalwidevelopment.kaprivatemessage.paper.command.PaperServersCommand;
import com.kalwidevelopment.kaprivatemessage.paper.gui.SoundSelectorGUI;
import com.kalwidevelopment.kaprivatemessage.paper.hook.EssentialsHook;
import com.kalwidevelopment.kaprivatemessage.paper.hook.LuckPermsHook;
import com.kalwidevelopment.kaprivatemessage.paper.hook.MiniPlaceholdersHook;
import com.kalwidevelopment.kaprivatemessage.paper.hook.PlaceholderAPIHook;
import com.kalwidevelopment.kaprivatemessage.paper.listener.PluginMessageListener;
import com.kalwidevelopment.kaprivatemessage.paper.listener.PlayerNicknameListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public class KAPrivateMessagePaper extends JavaPlugin {

    private EssentialsHook essentialsHook;
    private LuckPermsHook luckPermsHook;
    private SoundSelectorGUI soundSelectorGUI;
    private PluginMessageListener pluginMessageListener;
    private PaperPluginConfig paperPluginConfig;
    private PlayerNicknameListener playerNicknameListener;

    @Override
    public void onEnable() {
        paperPluginConfig = new PaperPluginConfig(this);
        paperPluginConfig.load();

        essentialsHook = new EssentialsHook(this);
        essentialsHook.setup();
        luckPermsHook = new LuckPermsHook(this);
        luckPermsHook.setup();

        soundSelectorGUI = new SoundSelectorGUI(this);

        Messenger messenger = getServer().getMessenger();
        pluginMessageListener = new PluginMessageListener(this, essentialsHook, soundSelectorGUI);
        messenger.registerIncomingPluginChannel(this, Constants.PLUGIN_CHANNEL, pluginMessageListener);
        messenger.registerOutgoingPluginChannel(this, Constants.PLUGIN_CHANNEL);

        playerNicknameListener = new PlayerNicknameListener(this, essentialsHook, luckPermsHook);
        getServer().getPluginManager().registerEvents(playerNicknameListener, this);
        getServer().getPluginManager().registerEvents(soundSelectorGUI, this);
        if (getCommand("msg") != null) {
            getCommand("msg").setExecutor(new PaperMessageCommand(this));
        }
        if (getCommand("pmservers") != null) {
            getCommand("pmservers").setExecutor(new PaperServersCommand(this));
        }
        if (getCommand("pmreload") != null) {
            getCommand("pmreload").setExecutor(new PaperReloadCommand(this));
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI hook registered.");
        }

        if (getServer().getPluginManager().getPlugin("MiniPlaceholders") != null) {
            MiniPlaceholdersHook.register(this);
            getLogger().info("MiniPlaceholders hook registered.");
        }

        getLogger().info("KAPrivateMessage (Paper) v1.0.0 enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("KAPrivateMessage (Paper) disabled.");
    }

    public EssentialsHook getEssentialsHook() { return essentialsHook; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public SoundSelectorGUI getSoundSelectorGUI() { return soundSelectorGUI; }
    public PaperPluginConfig getPaperPluginConfig() { return paperPluginConfig; }

    public void sendPluginMessage(byte[] data) {
        getServer().getOnlinePlayers().stream().findFirst().ifPresent(player ->
            player.sendPluginMessage(this, Constants.PLUGIN_CHANNEL, data)
        );
    }

    public void reloadPlugin() {
        reloadConfig();
        paperPluginConfig.load();
        essentialsHook.setup();
        luckPermsHook.setup();
        soundSelectorGUI.reloadFromConfig();
        for (Player player : getServer().getOnlinePlayers()) {
            playerNicknameListener.sendNicknameUpdate(player);
        }
    }
}
