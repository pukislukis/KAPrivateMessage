package com.kalwidevelopment.kaprivatemessage.paper.hook;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniPlaceholdersHook {

    public static void register(JavaPlugin plugin) {
        Expansion expansion = Expansion.builder("pm")
            .audiencePlaceholder("status", (audience, queue, ctx) ->
                Tag.inserting(Component.text("Active")))
            .audiencePlaceholder("plugin", (audience, queue, ctx) ->
                Tag.inserting(Component.text("KAPrivateMessage")))
            .build();
        expansion.register();
    }
}
