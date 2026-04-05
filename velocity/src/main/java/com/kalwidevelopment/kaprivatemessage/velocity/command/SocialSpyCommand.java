package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.List;

public class SocialSpyCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public SocialSpyCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(MM.deserialize("<red>Only players can use this command."));
            return;
        }

        if (!player.hasPermission(Constants.PERM_SOCIALSPY) && !player.hasPermission(Constants.PERM_ADMIN)) {
            player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-no-permission")));
            return;
        }

        String[] args = invocation.arguments();
        boolean enable;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("on")) {
                plugin.getSocialSpyManager().add(player.getUniqueId());
                enable = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                plugin.getSocialSpyManager().remove(player.getUniqueId());
                enable = false;
            } else {
                enable = plugin.getSocialSpyManager().toggle(player.getUniqueId());
            }
        } else {
            enable = plugin.getSocialSpyManager().toggle(player.getUniqueId());
        }

        if (enable) {
            player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("socialspy-on")));
        } else {
            player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("socialspy-off")));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return Arrays.asList("on", "off");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Constants.PERM_SOCIALSPY) || invocation.source().hasPermission(Constants.PERM_ADMIN);
    }
}
