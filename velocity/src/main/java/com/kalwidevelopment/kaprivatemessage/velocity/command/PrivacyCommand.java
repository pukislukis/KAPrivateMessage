package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.PrivacyLevel;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.List;

public class PrivacyCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public PrivacyCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(MM.deserialize("<red>Only players can use this command."));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 1) {
            sendHelp(player);
            return;
        }

        String levelStr = args[0].toUpperCase();
        if (!levelStr.equals("HIGH") && !levelStr.equals("MEDIUM") && !levelStr.equals("LOW") && !levelStr.equals("NONE")) {
            sendHelp(player);
            return;
        }

        PrivacyLevel level = PrivacyLevel.fromString(levelStr);
        plugin.getPlayerDataManager().setPrivacy(player.getUniqueId(), level);
        player.sendMessage(MessageFormatter.parse(
            plugin.getPluginConfig().getMessage("privacy-updated"),
            "{mode}", level.name()
        ));
    }

    private void sendHelp(Player player) {
        player.sendMessage(MM.deserialize("<yellow>Usage: <white>/pmprivacy <High|Medium|Low|None>"));
        player.sendMessage(MM.deserialize("<gray>High: <#ff1515>Block All"));
        player.sendMessage(MM.deserialize("<gray>Medium: <gold>Staff Only"));
        player.sendMessage(MM.deserialize("<gray>Low: <green>Staff & Donators"));
        player.sendMessage(MM.deserialize("<gray>None: <white>Everyone"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return Arrays.asList("High", "Medium", "Low", "None");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
