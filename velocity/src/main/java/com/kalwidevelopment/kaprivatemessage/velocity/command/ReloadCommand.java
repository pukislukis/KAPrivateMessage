package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements SimpleCommand {
    private final KAPrivateMessageVelocity plugin;

    public ReloadCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!hasPermission(invocation)) {
            invocation.source().sendMessage(MessageFormatter.parse(plugin.getPluginConfig().getMessage("error-no-permission")));
            return;
        }
        plugin.reloadPlugin();
        invocation.source().sendMessage(MessageFormatter.parse("<green>KAPrivateMessage Velocity reloaded."));
        invocation.source().sendMessage(MessageFormatter.parse("<yellow>Note: jar hot-swap is not supported by Velocity API; plugin binary update still requires proxy restart."));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Constants.PERM_ADMIN);
    }
}
