package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public ReplyCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) {
            invocation.source().sendMessage(MM.deserialize("<red>Only players can use this command."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 1) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-usage-reply")));
            return;
        }

        Optional<UUID> replyTargetOpt = plugin.getPlayerDataManager().getReplyTarget(sender.getUniqueId());
        if (replyTargetOpt.isEmpty()) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-no-reply-target")));
            return;
        }

        Optional<Player> targetOpt = plugin.getServer().getPlayer(replyTargetOpt.get());
        if (targetOpt.isEmpty()) {
            sender.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-player-offline")));
            return;
        }

        Player target = targetOpt.get();
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = target.getUsername();
        System.arraycopy(args, 0, newArgs, 1, args.length);

        final String[] finalNewArgs = newArgs;
        new MessageCommand(plugin).execute(new SimpleCommand.Invocation() {
            @Override
            public CommandSource source() { return sender; }
            @Override
            public String alias() { return "msg"; }
            @Override
            public String[] arguments() { return finalNewArgs; }
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
