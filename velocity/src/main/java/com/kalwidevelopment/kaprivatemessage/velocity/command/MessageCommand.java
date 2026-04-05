package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PrivacyLevel;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.kalwidevelopment.kaprivatemessage.velocity.util.PlayerFinder;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
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

        Optional<Player> targetOpt = PlayerFinder.find(plugin, targetName);
        if (targetOpt.isEmpty()) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-player-not-found")));
            return;
        }
        Player target = targetOpt.get();

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-cannot-msg-self")));
            return;
        }

        if (plugin.getAntiSpamManager().isOnCooldown(sender.getUniqueId())) {
            long remaining = plugin.getAntiSpamManager().getRemainingSeconds(sender.getUniqueId());
            sender.sendMessage(MessageFormatter.parse(
                plugin.getPluginConfig().getMessage("cooldown"),
                "{seconds}", String.valueOf(remaining)
            ));
            return;
        }

        if (plugin.getPlayerDataManager().isIgnoring(target.getUniqueId(), sender.getUniqueId())) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-ignored-by-target")));
            return;
        }

        if (plugin.getPlayerDataManager().isIgnoring(sender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-ignoring-target")));
            return;
        }

        if (!canSendPM(sender, target)) {
            PrivacyLevel mode = plugin.getPlayerDataManager().getPrivacy(target.getUniqueId());
            sender.sendMessage(MessageFormatter.parse(
                plugin.getPluginConfig().getMessage("error-privacy-restricted"),
                "{mode}", mode.name()
            ));
            return;
        }

        sender.sendMessage(MessageFormatter.formatSendMessage(plugin, sender, target, message));
        target.sendMessage(MessageFormatter.formatReceiveMessage(plugin, sender, target, message));

        Component spyMsg = MessageFormatter.formatSpyMessage(plugin, sender, target, message);
        for (UUID spyUUID : plugin.getSocialSpyManager().getSpies()) {
            if (spyUUID.equals(sender.getUniqueId()) || spyUUID.equals(target.getUniqueId())) continue;
            plugin.getServer().getPlayer(spyUUID).ifPresent(spy -> spy.sendMessage(spyMsg));
        }

        plugin.getPlayerDataManager().setReplyTarget(sender.getUniqueId(), target.getUniqueId());

        plugin.getMessageBridge().playSound(sender, "send");
        plugin.getMessageBridge().playSound(target, "receive");

        plugin.getAntiSpamManager().recordMessage(sender.getUniqueId());
    }

    private boolean canSendPM(Player sender, Player target) {
        PrivacyLevel mode = plugin.getPlayerDataManager().getPrivacy(target.getUniqueId());
        // HIGH: block all PMs except staff
        if (mode == PrivacyLevel.HIGH) {
            return sender.hasPermission(Constants.PERM_STAFF);
        }
        // MEDIUM: only staff can send
        if (mode == PrivacyLevel.MEDIUM) {
            return sender.hasPermission(Constants.PERM_STAFF);
        }
        // LOW: staff and donators can send
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
