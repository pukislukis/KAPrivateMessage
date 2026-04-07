package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.stream.Collectors;

public class SoundCommand implements SimpleCommand {

    private final KAPrivateMessageVelocity plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public SoundCommand(KAPrivateMessageVelocity plugin) {
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

        switch (args[0].toLowerCase()) {
            case "toggle" -> {
                if (args.length < 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                    player.sendMessage(MM.deserialize("<#ff1515>Usage: <gray>/pmsound toggle <on/off>"));
                    return;
                }
                boolean on = args[1].equalsIgnoreCase("on");

                Player target = player;
                if (args.length >= 3 && player.hasPermission(Constants.PERM_ADMIN)) {
                    Optional<Player> targetOpt = plugin.getServer().getPlayer(args[2]);
                    if (targetOpt.isEmpty()) {
                        player.sendMessage(MM.deserialize("<#ff1515>Player not found."));
                        return;
                    }
                    target = targetOpt.get();
                }

                plugin.getPlayerDataManager().setSoundEnabled(target.getUniqueId(), on);
                if (on) {
                    player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("sound-toggle-on")));
                } else {
                    player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("sound-toggle-off")));
                }
            }
            case "gui" -> {
                plugin.getMessageBridge().sendToServer(player,
                    PacketUtil.requestSettingsGuiPacket(player.getUniqueId().toString()));
                player.sendMessage(MessageFormatter.parse("<green>Opening Private Message Settings GUI..."));
            }
            case "setother" -> {
                if (!player.hasPermission(Constants.PERM_ADMIN)) {
                    player.sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-no-permission")));
                    return;
                }
                if (args.length < 4) {
                    player.sendMessage(MM.deserialize("<#ff1515>Usage: <gray>/pmsound setother <receive/send> <player> <sound_id> [volume] [pitch]"));
                    return;
                }
                String soundType = args[1].toLowerCase();
                if (!soundType.equals("send") && !soundType.equals("receive")) {
                    player.sendMessage(MM.deserialize("<#ff1515>Sound type must be 'send' or 'receive'."));
                    return;
                }
                Optional<Player> targetOpt = plugin.getServer().getPlayer(args[2]);
                if (targetOpt.isEmpty()) {
                    player.sendMessage(MM.deserialize("<#ff1515>Player not found."));
                    return;
                }
                Player target = targetOpt.get();
                String soundId = args[3];
                double volume = args.length >= 5 ? parseDouble(args[4], 1.0) : 1.0;
                double pitch = args.length >= 6 ? parseDouble(args[5], 1.0) : 1.0;
                plugin.getPlayerDataManager().setSoundSettings(target.getUniqueId(), soundType, soundId, volume, pitch);
                player.sendMessage(MM.deserialize("<green>Updated <white>" + target.getUsername() + "'s <yellow>" + soundType + " <green>PM sound to <white>" + soundId + "<green>."));
            }
            default -> sendHelp(player);
        }
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return def; }
    }

    private void sendHelp(Player player) {
        player.sendMessage(MM.deserialize("<yellow>PM Sound Commands:"));
        player.sendMessage(MM.deserialize(" <gray>/pmsound toggle <on/off>"));
        player.sendMessage(MM.deserialize(" <gray>/pmsound gui"));
        if (player.hasPermission(Constants.PERM_ADMIN)) {
            player.sendMessage(MM.deserialize(" <gray>/pmsound setother <send/receive> <player> <sound_id> [vol] [pitch]"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) return Arrays.asList("toggle", "gui", "setother");
        if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length == 2) return Arrays.asList("on", "off");
        }
        if (args[0].equalsIgnoreCase("setother") && invocation.source() instanceof Player p && p.hasPermission(Constants.PERM_ADMIN)) {
            if (args.length == 2) return Arrays.asList("send", "receive");
            if (args.length == 3) return plugin.getServer().getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
