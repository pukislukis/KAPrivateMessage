package com.kalwidevelopment.kaprivatemessage.paper.command;

import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaperServersCommand implements CommandExecutor {
    private final KAPrivateMessagePaper plugin;

    public PaperServersCommand(KAPrivateMessagePaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        byte[] data = PacketUtil.toBytes(PacketUtil.pmServersRequestPacket(player.getUniqueId().toString()));
        player.sendPluginMessage(plugin, com.kalwidevelopment.kaprivatemessage.common.Constants.PLUGIN_CHANNEL, data);
        player.sendMessage("Requesting connected proxy backend servers...");
        return true;
    }
}
