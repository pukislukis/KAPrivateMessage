package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PrivacyLevel;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.DebugLogger;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageColorUtil;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.kalwidevelopment.kaprivatemessage.velocity.util.PlayerFinder;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public MessageCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) {
            invocation.source().sendMessage(MM.deserialize("<red>Only players can use this command."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-usage-msg")));
            return;
        }

        String targetName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        executeAs(sender, targetName, message, "velocity-command");
    }

    public void executeAs(Player sender, String targetName, String message, String source) {
        DebugLogger.log(plugin, "PM request source=" + source + " sender=" + sender.getUsername() + " targetQuery=" + targetName);

        Optional<Player> targetOpt = PlayerFinder.find(plugin, targetName, sender);
        if (targetOpt.isEmpty()) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-player-not-found")));
            DebugLogger.log(plugin, "PM failed: target not found sender=" + sender.getUsername() + " query=" + targetName);
            return;
        }
        Player target = targetOpt.get();

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-cannot-msg-self")));
            DebugLogger.log(plugin, "PM blocked self-message sender=" + sender.getUsername() + " target=" + target.getUsername());
            return;
        }

        if (plugin.getAntiSpamManager().isOnCooldown(sender.getUniqueId())) {
            long remaining = plugin.getAntiSpamManager().getRemainingSeconds(sender.getUniqueId());
            sender.sendMessage(MessageFormatter.parse(
                plugin.getPluginConfig().getMessage("cooldown"),
                "{seconds}", String.valueOf(remaining)
            ));
            DebugLogger.log(plugin, "PM blocked cooldown sender=" + sender.getUsername() + " remaining=" + remaining + "s");
            return;
        }

        if (plugin.getPlayerDataManager().isIgnoring(target.getUniqueId(), sender.getUniqueId())) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-ignored-by-target")));
            DebugLogger.log(plugin, "PM blocked ignored-by-target sender=" + sender.getUsername() + " target=" + target.getUsername());
            return;
        }

        if (plugin.getPlayerDataManager().isIgnoring(sender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-ignoring-target")));
            DebugLogger.log(plugin, "PM blocked sender-ignoring-target sender=" + sender.getUsername() + " target=" + target.getUsername());
            return;
        }

        if (!canSendPM(sender, target)) {
            PrivacyLevel mode = plugin.getPlayerDataManager().getPrivacy(target.getUniqueId());
            sender.sendMessage(MessageFormatter.parse(
                plugin.getPluginConfig().getMessage("error-privacy-restricted"),
                "{mode}", mode.name()
            ));
            DebugLogger.log(plugin, "PM blocked privacy sender=" + sender.getUsername() + " target=" + target.getUsername() + " mode=" + mode.name());
            return;
        }

        boolean canUseColors = sender.hasPermission(Constants.PERM_PM_COLOR);
        String finalMessage = MessageColorUtil.applyPlayerMessageColors(message, canUseColors);

        sender.sendMessage(MessageFormatter.formatSendMessage(plugin, sender, target, finalMessage));
        target.sendMessage(MessageFormatter.formatReceiveMessage(plugin, sender, target, finalMessage));

        Component spyMsg = MessageFormatter.formatSpyMessage(plugin, sender, target, finalMessage);
        for (UUID spyUUID : plugin.getSocialSpyManager().getSpies()) {
            if (spyUUID.equals(sender.getUniqueId()) || spyUUID.equals(target.getUniqueId())) continue;
            plugin.getServer().getPlayer(spyUUID).ifPresent(spy -> {
                spy.sendMessage(spyMsg);
                DebugLogger.log(plugin, "Spy delivered to=" + spy.getUsername() + " sender=" + sender.getUsername() + " target=" + target.getUsername());
            });
        }

        plugin.getPlayerDataManager().setReplyTarget(sender.getUniqueId(), target.getUniqueId());

        plugin.getMessageBridge().playSound(sender, "send");
        plugin.getMessageBridge().playSound(target, "receive");

        plugin.getAntiSpamManager().recordMessage(sender.getUniqueId());
        DebugLogger.log(plugin, "PM success sender=" + sender.getUsername() + " target=" + target.getUsername());
    }

    private boolean canSendPM(Player sender, Player target) {
        PrivacyLevel mode = plugin.getPlayerDataManager().getPrivacy(target.getUniqueId());
        if (mode == PrivacyLevel.HIGH) {
            return sender.hasPermission(Constants.PERM_STAFF);
        }
        if (mode == PrivacyLevel.MEDIUM) {
            return sender.hasPermission(Constants.PERM_STAFF);
        }
        if (mode == PrivacyLevel.LOW) {
            return sender.hasPermission(Constants.PERM_STAFF) || sender.hasPermission(Constants.PERM_DONATOR);
        }
        return true;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length <= 1) {
            String prefix = invocation.arguments().length == 1 ? invocation.arguments()[0].toLowerCase() : "";
            return plugin.getServer().getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
