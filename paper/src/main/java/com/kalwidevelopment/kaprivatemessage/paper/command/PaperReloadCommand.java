package com.kalwidevelopment.kaprivatemessage.paper.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaperReloadCommand implements CommandExecutor {
    private final KAPrivateMessagePaper plugin;

    public PaperReloadCommand(KAPrivateMessagePaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Constants.PERM_ADMIN)) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("hard")) {
            sender.sendMessage("§eRunning full server reload to refresh plugin jar...");
            Bukkit.reload();
            return true;
        }

        plugin.reloadPlugin();
        sender.sendMessage("§aKAPrivateMessage Paper reloaded.");
        sender.sendMessage("§eUse /pmreload hard if you also need to refresh jar binaries without full server restart.");
        return true;
    }
}
