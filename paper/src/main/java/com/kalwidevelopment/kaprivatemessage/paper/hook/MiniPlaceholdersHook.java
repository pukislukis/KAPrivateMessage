package com.kalwidevelopment.kaprivatemessage.paper.hook;

import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

public class MiniPlaceholdersHook {

    public static void register(KAPrivateMessagePaper plugin) {
        Expansion expansion = Expansion.builder("pm")
            .audiencePlaceholder("status", (audience, queue, ctx) ->
                Tag.inserting(Component.text("Active")))
            .audiencePlaceholder("plugin", (audience, queue, ctx) ->
                Tag.inserting(Component.text("KAPrivateMessage")))
            .audiencePlaceholder("server_id", (audience, queue, ctx) ->
                Tag.inserting(Component.text(plugin.getPaperPluginConfig().getServerId())))
            .audiencePlaceholder("server_name", (audience, queue, ctx) ->
                Tag.inserting(Component.text(plugin.getPaperPluginConfig().getServerName())))
            .audiencePlaceholder("server_formatted", (audience, queue, ctx) ->
                Tag.inserting(Component.text(plugin.getPaperPluginConfig().getServerFormatted())))
            .build();
        expansion.register();
    }
}
