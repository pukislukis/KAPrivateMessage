package com.kalwidevelopment.kaprivatemessage.paper.gui;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SoundSelectorGUI implements Listener {

    private final KAPrivateMessagePaper plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String[][] SOUNDS = {
        {"block.note_block.hat", "Note Block Hat", "LIME_STAINED_GLASS_PANE"},
        {"block.note_block.bell", "Note Block Bell", "YELLOW_STAINED_GLASS_PANE"},
        {"block.note_block.chime", "Note Block Chime", "CYAN_STAINED_GLASS_PANE"},
        {"block.note_block.flute", "Note Block Flute", "BLUE_STAINED_GLASS_PANE"},
        {"entity.experience_orb.pickup", "XP Orb Pickup", "GREEN_STAINED_GLASS_PANE"},
        {"entity.player.levelup", "Level Up", "GOLD_INGOT"},
        {"entity.villager.yes", "Villager Yes", "EMERALD"},
        {"ui.button.click", "Button Click", "STONE_BUTTON"},
        {"block.amethyst_block.hit", "Amethyst Hit", "AMETHYST_SHARD"},
        {"entity.enderman.teleport", "Enderman Teleport", "ENDER_PEARL"},
        {"entity.item.pickup", "Item Pickup", "PAPER"},
        {"block.note_block.pling", "Note Block Pling", "MUSIC_DISC_CAT"},
    };

    private final Map<UUID, String> pendingSelection = new HashMap<>();
    private final Map<UUID, Inventory> openInventories = new HashMap<>();

    public SoundSelectorGUI(KAPrivateMessagePaper plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text("Select Sound Type", NamedTextColor.GOLD));

        ItemStack sendItem = createItem(Material.PAPER, "<#FF0090><b>Send Sound</b>", "<gray>Choose sound for when you send a PM");
        ItemStack receiveItem = createItem(Material.BOOK, "<#FF0090><b>Receive Sound</b>", "<gray>Choose sound for when you receive a PM");

        inv.setItem(2, sendItem);
        inv.setItem(6, receiveItem);

        pendingSelection.put(player.getUniqueId(), "type-select");
        openInventories.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    public void openSoundList(Player player, String soundType) {
        int rows = (SOUNDS.length / 9) + 2;
        Inventory inv = Bukkit.createInventory(null, rows * 9,
            Component.text("Select " + soundType + " Sound", NamedTextColor.GOLD));

        for (int i = 0; i < SOUNDS.length; i++) {
            String[] s = SOUNDS[i];
            Material mat;
            try { mat = Material.valueOf(s[2]); } catch (IllegalArgumentException e) { mat = Material.MUSIC_DISC_CAT; }
            ItemStack item = createItem(mat, "<white>" + s[1], "<gray>Sound ID: <white>" + s[0]);
            inv.setItem(i, item);
        }

        pendingSelection.put(player.getUniqueId(), soundType);
        openInventories.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Inventory tracked = openInventories.get(uuid);
        if (tracked == null || !event.getInventory().equals(tracked)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        String pending = pendingSelection.get(uuid);
        if (pending == null) return;

        if ("type-select".equals(pending)) {
            int slot = event.getSlot();
            if (slot == 2) {
                player.closeInventory();
                openSoundList(player, "send");
            } else if (slot == 6) {
                player.closeInventory();
                openSoundList(player, "receive");
            }
            return;
        }

        int slot = event.getSlot();
        if (slot >= 0 && slot < SOUNDS.length) {
            String soundId = SOUNDS[slot][0];
            double volume = 1.0;
            double pitch = 1.0;

            try {
                Sound sound = Sound.valueOf(soundId.toUpperCase().replace(".", "_").replace(":", "_"));
                player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
            } catch (IllegalArgumentException e) {
                player.playSound(player.getLocation(), soundId, (float) volume, (float) pitch);
            }

            byte[] data = PacketUtil.toBytes(PacketUtil.soundSelectedPacket(
                uuid.toString(), pending, soundId, volume, pitch
            ));
            player.sendPluginMessage(plugin, Constants.PLUGIN_CHANNEL, data);

            player.sendMessage(MM.deserialize("<green>Sound updated to <white>" + SOUNDS[slot][1] + "<green>!"));
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        openInventories.remove(player.getUniqueId());
        pendingSelection.remove(player.getUniqueId());
    }

    private ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MM.deserialize(name));
            meta.lore(Collections.singletonList(MM.deserialize(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }
}
