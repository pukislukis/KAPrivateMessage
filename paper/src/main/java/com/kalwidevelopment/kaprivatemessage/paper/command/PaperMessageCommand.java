package com.kalwidevelopment.kaprivatemessage.paper.command;

import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaperMessageCommand implements CommandExecutor {
    private final KAPrivateMessagePaper plugin;

    public PaperMessageCommand(KAPrivateMessagePaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("Usage: /msg <player> <message>");
            return true;
        }
        String targetName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        byte[] data = PacketUtil.toBytes(PacketUtil.pmSendRequestPacket(player.getUniqueId().toString(), targetName, message));
        player.sendPluginMessage(plugin, com.kalwidevelopment.kaprivatemessage.common.Constants.PLUGIN_CHANNEL, data);
        return true;
    }
}
