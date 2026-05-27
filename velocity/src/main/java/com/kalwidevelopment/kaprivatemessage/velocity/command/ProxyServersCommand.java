package com.kalwidevelopment.kaprivatemessage.velocity.command;

import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;
import com.kalwidevelopment.kaprivatemessage.velocity.util.MessageFormatter;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyServersCommand implements SimpleCommand {
    private final KAPrivateMessageVelocity plugin;

    public ProxyServersCommand(KAPrivateMessageVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        List<RegisteredServer> servers = plugin.getServer().getAllServers().stream()
            .sorted(Comparator.comparing(s -> s.getServerInfo().getName()))
            .collect(Collectors.toList());

        invocation.source().sendMessage(MessageFormatter.parse("<gold>Connected backend servers (" + servers.size() + "):"));
        for (RegisteredServer server : servers) {
            int playerCount = server.getPlayersConnected().size();
            invocation.source().sendMessage(MessageFormatter.parse(
                "<gray>- <white>" + server.getServerInfo().getName() + "<gray> : <aqua>" + playerCount + " player(s)"
            ));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
