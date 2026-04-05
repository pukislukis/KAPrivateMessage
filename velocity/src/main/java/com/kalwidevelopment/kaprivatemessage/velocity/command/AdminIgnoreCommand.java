package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.PlayerDataManager;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.stream.Collectors;

public class AdminIgnoreCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public AdminIgnoreCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission(Constants.PERM_ADMIN)) {
            invocation.source().sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-no-permission")));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            invocation.source().sendMessage(MM.deserialize("<yellow>Usage: <white>/adminignore <list|clear> <player>"));
            return;
        }

        Optional<Player> targetOpt = plugin.getServer().getPlayer(args[1]);
        if (targetOpt.isEmpty()) {
            invocation.source().sendMessage(MM.deserialize("<#ff1515>Player not found or is offline."));
            return;
        }
        Player target = targetOpt.get();
        PlayerDataManager dm = plugin.getPlayerDataManager();

        switch (args[0].toLowerCase()) {
            case "list" -> {
                Set<UUID> ignoreList = dm.getIgnoreList(target.getUniqueId());
                invocation.source().sendMessage(MM.deserialize("<gold>Ignore List of " + target.getUsername() + ":"));
                for (UUID uuid : ignoreList) {
                    String name = plugin.getServer().getPlayer(uuid).map(Player::getUsername).orElse(uuid.toString());
                    invocation.source().sendMessage(MM.deserialize(" <gray>- <white>" + name));
                }
            }
            case "clear" -> {
                dm.clearIgnore(target.getUniqueId());
                invocation.source().sendMessage(MM.deserialize("<green>Successfully cleared ignore list of " + target.getUsername() + "."));
            }
            default -> invocation.source().sendMessage(MM.deserialize("<yellow>Usage: <white>/adminignore <list|clear> <player>"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) return Arrays.asList("list", "clear");
        if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            return plugin.getServer().getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(n -> n.toLowerCase().startsWith(prefix))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Constants.PERM_ADMIN);
    }
}
