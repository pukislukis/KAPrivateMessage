package com.kalwidevelopment.kaprivatemessage.paper;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.paper.gui.SoundSelectorGUI;
import com.kalwidevelopment.kaprivatemessage.paper.hook.EssentialsHook;
import com.kalwidevelopment.kaprivatemessage.paper.hook.MiniPlaceholdersHook;
import com.kalwidevelopment.kaprivatemessage.paper.hook.PlaceholderAPIHook;
import com.kalwidevelopment.kaprivatemessage.paper.listener.PluginMessageListener;
import com.kalwidevelopment.kaprivatemessage.paper.listener.PlayerNicknameListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public class KAPrivateMessagePaper extends JavaPlugin {

    private EssentialsHook essentialsHook;
    private SoundSelectorGUI soundSelectorGUI;
    private PluginMessageListener pluginMessageListener;
    private PaperPluginConfig paperPluginConfig;

    @Override
    public void onEnable() {
        paperPluginConfig = new PaperPluginConfig(this);
        paperPluginConfig.load();

        essentialsHook = new EssentialsHook(this);
        essentialsHook.setup();

        soundSelectorGUI = new SoundSelectorGUI(this);

        Messenger messenger = getServer().getMessenger();
        pluginMessageListener = new PluginMessageListener(this, essentialsHook, soundSelectorGUI);
        messenger.registerIncomingPluginChannel(this, Constants.PLUGIN_CHANNEL, pluginMessageListener);
        messenger.registerOutgoingPluginChannel(this, Constants.PLUGIN_CHANNEL);

        getServer().getPluginManager().registerEvents(new PlayerNicknameListener(this, essentialsHook), this);
        getServer().getPluginManager().registerEvents(soundSelectorGUI, this);

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
    public SoundSelectorGUI getSoundSelectorGUI() { return soundSelectorGUI; }
    public PaperPluginConfig getPaperPluginConfig() { return paperPluginConfig; }

    public void sendPluginMessage(byte[] data) {
        getServer().getOnlinePlayers().stream().findFirst().ifPresent(player ->
            player.sendPluginMessage(this, Constants.PLUGIN_CHANNEL, data)
        );
    }
}
