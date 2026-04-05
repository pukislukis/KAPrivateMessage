package com.kalwidevelopment.kaprivatemessage.velocity;

import com.google.inject.Inject;
import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.velocity.command.*;
import com.kalwidevelopment.kaprivatemessage.velocity.hook.LuckPermsHook;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.AntiSpamManager;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.PlayerDataManager;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.SocialSpyManager;
import com.kalwidevelopment.kaprivatemessage.velocity.messaging.VelocityMessageBridge;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "kaprivatemessage",
    name = "KAPrivateMessage",
    version = "1.0.0",
    description = "Cross-platform Private Message plugin",
    authors = {"KalwiDevelopment"}
)
public class KAPrivateMessageVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private PluginConfig config;
    private PlayerDataManager playerDataManager;
    private SocialSpyManager socialSpyManager;
    private AntiSpamManager antiSpamManager;
    private VelocityMessageBridge messageBridge;
    private LuckPermsHook luckPermsHook;

    public static final MinecraftChannelIdentifier CHANNEL =
        MinecraftChannelIdentifier.from(Constants.PLUGIN_CHANNEL);

    @Inject
    public KAPrivateMessageVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.config = new PluginConfig(dataDirectory, logger);
        this.config.load();

        this.playerDataManager = new PlayerDataManager(dataDirectory, logger);
        this.playerDataManager.load();
        this.socialSpyManager = new SocialSpyManager();
        this.antiSpamManager = new AntiSpamManager(config.getCooldownSeconds());
        this.luckPermsHook = new LuckPermsHook(logger);
        this.luckPermsHook.setup();

        this.messageBridge = new VelocityMessageBridge(server, logger);
        this.messageBridge.setPlugin(this);

        server.getChannelRegistrar().register(CHANNEL);

        server.getEventManager().register(this, messageBridge);
        server.getEventManager().register(this, new PlayerQuitListener(playerDataManager, socialSpyManager, antiSpamManager));

        registerCommands();

        logger.info("KAPrivateMessage (Velocity) v1.0.0 loaded!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (playerDataManager != null) {
            playerDataManager.save();
        }
        logger.info("KAPrivateMessage (Velocity) disabled.");
    }

    private void registerCommands() {
        MessageCommand msgCmd = new MessageCommand(this);
        ReplyCommand replyCmd = new ReplyCommand(this);
        PrivacyCommand privacyCmd = new PrivacyCommand(this);
        SoundCommand soundCmd = new SoundCommand(this);
        IgnoreCommand ignoreCmd = new IgnoreCommand(this);
        AdminIgnoreCommand adminIgnoreCmd = new AdminIgnoreCommand(this);
        SocialSpyCommand socialSpyCmd = new SocialSpyCommand(this);
        ProxyServersCommand proxyServersCommand = new ProxyServersCommand(this);

        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("msg")
                .aliases("w", "tell", "pm", "m", "t", "whisper").build(),
            msgCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("reply")
                .aliases("r").build(),
            replyCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("pmprivacy").build(),
            privacyCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("pmsound").build(),
            soundCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("ignore").build(),
            ignoreCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("adminignore").build(),
            adminIgnoreCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("socialspy").build(),
            socialSpyCmd
        );
        server.getCommandManager().register(
            server.getCommandManager().metaBuilder("pmservers").build(),
            proxyServersCommand
        );
    }

    public ProxyServer getServer() { return server; }
    public Logger getLogger() { return logger; }
    public PluginConfig getPluginConfig() { return config; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public SocialSpyManager getSocialSpyManager() { return socialSpyManager; }
    public AntiSpamManager getAntiSpamManager() { return antiSpamManager; }
    public VelocityMessageBridge getMessageBridge() { return messageBridge; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
}
