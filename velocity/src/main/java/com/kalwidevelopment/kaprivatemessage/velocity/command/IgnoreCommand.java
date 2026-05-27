package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.manager.PlayerDataManager;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.stream.Collectors;

public class IgnoreCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public IgnoreCommand(KAPrivateMessageVelocity plugin) {
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

        PlayerDataManager dm = plugin.getPlayerDataManager();

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<#ff1515>Please specify a player to ignore."));
                    return;
                }
                Optional<Player> targetOpt = plugin.getServer().getPlayer(args[1]);
                if (targetOpt.isEmpty()) {
                    player.sendMessage(MM.deserialize("<#ff1515>Player not found or is offline."));
                    return;
                }
                Player target = targetOpt.get();
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("ignore-cannot-self")));
                    return;
                }
                if (dm.isIgnoring(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage(MessageFormatter.parse(
                        plugin.getPluginConfig().getMessage("ignore-already"),
                        "{player}", target.getUsername()
                    ));
                    return;
                }
                dm.addIgnore(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(MessageFormatter.parse(
                    plugin.getPluginConfig().getMessage("ignore-added"),
                    "{player}", target.getUsername()
                ));
            }
            case "remove" -> {
                if (args.length < 2) {
                    player.sendMessage(MM.deserialize("<#ff1515>Please specify a player to remove from ignore list."));
                    return;
                }
                Optional<Player> targetOpt = plugin.getServer().getPlayer(args[1]);
                if (targetOpt.isEmpty()) {
                    player.sendMessage(MM.deserialize("<#ff1515>Player not found or is offline."));
                    return;
                }
                Player target = targetOpt.get();
                if (!dm.isIgnoring(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage(MessageFormatter.parse(
                        plugin.getPluginConfig().getMessage("ignore-not-in-list"),
                        "{player}", target.getUsername()
                    ));
                    return;
                }
                dm.removeIgnore(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(MessageFormatter.parse(
                    plugin.getPluginConfig().getMessage("ignore-removed"),
                    "{player}", target.getUsername()
                ));
            }
            case "list" -> {
                Set<UUID> ignoreList = dm.getIgnoreList(player.getUniqueId());
                if (ignoreList.isEmpty()) {
                    player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("ignore-list-empty")));
                    return;
                }
                player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("ignore-list-header")));
                for (UUID uuid : ignoreList) {
                    String name = plugin.getServer().getPlayer(uuid)
                        .map(Player::getUsername)
                        .orElse(uuid.toString());
                    player.sendMessage(MessageFormatter.parse(
                        plugin.getPluginConfig().getMessage("ignore-list-entry"),
                        "{player}", name
                    ));
                }
            }
            case "clear" -> {
                dm.clearIgnore(player.getUniqueId());
                player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("ignore-cleared")));
            }
            default -> sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(MM.deserialize("<yellow>Usage: <white>/ignore <add|remove|list|clear> [player]"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) return Arrays.asList("add", "remove", "list", "clear");
        if (args[0].equalsIgnoreCase("add") && args.length == 2) {
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
        return true;
    }
}
