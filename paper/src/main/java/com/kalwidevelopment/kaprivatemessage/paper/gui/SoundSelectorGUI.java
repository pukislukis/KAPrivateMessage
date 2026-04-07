package com.kalwidevelopment.kaprivatemessage.paper.gui;

import com.kalwidevelopment.kaprivatemessage.common.Constants;
import com.kalwidevelopment.kaprivatemessage.common.PacketUtil;
import com.kalwidevelopment.kaprivatemessage.paper.KAPrivateMessagePaper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SoundSelectorGUI implements Listener {

    private final KAPrivateMessagePaper plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<UUID, String> openMenu = new HashMap<>();
    private final Map<UUID, String> selectedSoundType = new HashMap<>();
    private final Map<UUID, Map<Integer, String>> slotActions = new HashMap<>();
    private final Map<UUID, Map<Integer, String>> slotSoundIds = new HashMap<>();

    public SoundSelectorGUI(KAPrivateMessagePaper plugin) {
        this.plugin = plugin;
    }

    public void reloadFromConfig() {
        // dynamic read from config on each open; no cached structures needed
    }

    public void openSettingsGUI(Player player) {
        openConfiguredMenu(player, "settings-main");
    }

    public void openGUI(Player player) {
        openSettingsGUI(player);
    }

    public void openSoundList(Player player, String soundType) {
        selectedSoundType.put(player.getUniqueId(), soundType);
        openSoundOptionsMenu(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Inventory tracked = openInventories.get(uuid);
        if (tracked == null || !event.getView().getTopInventory().equals(tracked)) return;

        event.setCancelled(true);
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= tracked.getSize()) return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        int slot = rawSlot;
        String action = slotActions.getOrDefault(uuid, Collections.emptyMap()).get(slot);
        if (action == null) return;

        switch (action.toUpperCase(Locale.ROOT)) {
            case "OPEN_SOUND_MENU" -> openConfiguredMenu(player, "settings-sound-type");
            case "OPEN_PRIVACY_MENU" -> openConfiguredMenu(player, "settings-privacy");
            case "OPEN_IGNORES_MENU" -> openConfiguredMenu(player, "settings-ignores");
            case "OPEN_MAIN_MENU" -> openConfiguredMenu(player, "settings-main");
            case "OPEN_SOUND_SEND" -> openSoundList(player, "send");
            case "OPEN_SOUND_RECEIVE" -> openSoundList(player, "receive");
            case "SET_PRIVACY_NONE" -> setPrivacy(player, "NONE");
            case "SET_PRIVACY_LOW" -> setPrivacy(player, "LOW");
            case "SET_PRIVACY_MEDIUM" -> setPrivacy(player, "MEDIUM");
            case "SET_PRIVACY_HIGH" -> setPrivacy(player, "HIGH");
            case "REQUEST_IGNORE_LIST" -> requestIgnoreList(player);
            case "CLEAR_IGNORES" -> clearIgnores(player);
            case "SELECT_SOUND" -> selectSound(player, slot);
            case "CLOSE" -> player.closeInventory();
            default -> {
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory tracked = openInventories.get(player.getUniqueId());
        if (tracked == null || !event.getView().getTopInventory().equals(tracked)) return;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < tracked.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        openInventories.remove(uuid);
        openMenu.remove(uuid);
        slotActions.remove(uuid);
        slotSoundIds.remove(uuid);
        selectedSoundType.remove(uuid);
    }

    private void openConfiguredMenu(Player player, String menuKey) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menu = cfg.getConfigurationSection("gui.menus." + menuKey);
        if (menu == null) {
            player.sendMessage("§cGUI menu '" + menuKey + "' not found in config.");
            return;
        }

        int size = normalizeSize(menu.getInt("size", 27));
        String title = menu.getString("title", "Private Message Settings");
        Inventory inv = Bukkit.createInventory(null, size, nonItalic(MM.deserialize(title)));

        Map<Integer, String> actionMap = new HashMap<>();
        List<Map<?, ?>> items = menu.getMapList("items");
        for (Map<?, ?> itemMap : items) {
            int slot = toInt(itemMap.get("slot"), -1);
            if (slot < 0 || slot >= size) continue;
            Material mat = parseMaterial(getString(itemMap, "material", "GRAY_STAINED_GLASS_PANE"));
            String name = getString(itemMap, "name", "<white>Item");
            List<String> lore = toStringList(itemMap.get("lore"));
            String action = getString(itemMap, "action", "");
            inv.setItem(slot, createItem(mat, name, lore));
            actionMap.put(slot, action);
        }

        openInventories.put(player.getUniqueId(), inv);
        openMenu.put(player.getUniqueId(), menuKey);
        slotActions.put(player.getUniqueId(), actionMap);
        slotSoundIds.remove(player.getUniqueId());
        player.openInventory(inv);
    }

    private void openSoundOptionsMenu(Player player) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection menu = cfg.getConfigurationSection("gui.menus.settings-sound-options");
        if (menu == null) {
            player.sendMessage("§cGUI menu 'settings-sound-options' not found in config.");
            return;
        }

        int size = normalizeSize(menu.getInt("size", 54));
        String title = menu.getString("title", "<gold>Select PM Sound");
        Inventory inv = Bukkit.createInventory(null, size, nonItalic(MM.deserialize(title)));
        Map<Integer, String> actionMap = new HashMap<>();
        Map<Integer, String> soundMap = new HashMap<>();

        int index = 0;
        List<Map<?, ?>> sounds = cfg.getMapList("gui.sound-options");
        for (Map<?, ?> sound : sounds) {
            if (index >= size) break;
            String soundId = getString(sound, "id", "entity.experience_orb.pickup");
            String name = getString(sound, "name", "<white>" + soundId);
            String materialName = getString(sound, "material", "MUSIC_DISC_CAT");
            List<String> lore = toStringList(sound.get("lore"));
            if (lore.isEmpty()) {
                lore = Collections.singletonList("<gray>ID: <white>" + soundId);
            } else {
                lore = lore.stream().map(l -> l.replace("{sound-id}", soundId)).collect(Collectors.toList());
            }
            inv.setItem(index, createItem(parseMaterial(materialName), name, lore));
            actionMap.put(index, "SELECT_SOUND");
            soundMap.put(index, soundId);
            index++;
        }

        List<Map<?, ?>> staticItems = menu.getMapList("items");
        for (Map<?, ?> itemMap : staticItems) {
            int slot = toInt(itemMap.get("slot"), -1);
            if (slot < 0 || slot >= size) continue;
            Material mat = parseMaterial(getString(itemMap, "material", "BARRIER"));
            String name = getString(itemMap, "name", "<red>Back");
            List<String> lore = toStringList(itemMap.get("lore"));
            String action = getString(itemMap, "action", "");
            inv.setItem(slot, createItem(mat, name, lore));
            actionMap.put(slot, action);
        }

        openInventories.put(player.getUniqueId(), inv);
        openMenu.put(player.getUniqueId(), "settings-sound-options");
        slotActions.put(player.getUniqueId(), actionMap);
        slotSoundIds.put(player.getUniqueId(), soundMap);
        player.openInventory(inv);
    }

    private void selectSound(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        String soundId = slotSoundIds.getOrDefault(uuid, Collections.emptyMap()).get(slot);
        String soundType = selectedSoundType.get(uuid);
        if (soundId == null || soundType == null) return;

        double volume = 1.0;
        double pitch = 1.0;
        try {
            Sound sound = Sound.valueOf(soundId.toUpperCase(Locale.ROOT).replace(".", "_").replace(":", "_"));
            player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), soundId, (float) volume, (float) pitch);
        }

        byte[] data = PacketUtil.toBytes(PacketUtil.soundSelectedPacket(
            uuid.toString(), soundType, soundId, volume, pitch
        ));
        player.sendPluginMessage(plugin, Constants.PLUGIN_CHANNEL, data);
        player.sendMessage(MM.deserialize("<green>PM " + soundType + " sound updated to <white>" + soundId + "<green>."));
        player.closeInventory();
    }

    private void setPrivacy(Player player, String level) {
        byte[] data = PacketUtil.toBytes(PacketUtil.pmPrivacySetPacket(player.getUniqueId().toString(), level));
        player.sendPluginMessage(plugin, Constants.PLUGIN_CHANNEL, data);
        player.sendMessage(MM.deserialize("<green>PM privacy updated to <yellow>" + level + "<green>."));
        player.closeInventory();
    }

    private void requestIgnoreList(Player player) {
        byte[] data = PacketUtil.toBytes(PacketUtil.pmIgnoresListRequestPacket(player.getUniqueId().toString()));
        player.sendPluginMessage(plugin, Constants.PLUGIN_CHANNEL, data);
        player.sendMessage(MM.deserialize("<yellow>Requesting your PM ignore list..."));
        player.closeInventory();
    }

    private void clearIgnores(Player player) {
        byte[] data = PacketUtil.toBytes(PacketUtil.pmIgnoresClearPacket(player.getUniqueId().toString()));
        player.sendPluginMessage(plugin, Constants.PLUGIN_CHANNEL, data);
        player.sendMessage(MM.deserialize("<green>Your PM ignore list has been cleared."));
        player.closeInventory();
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(nonItalic(MM.deserialize(name)));
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComp = lore.stream().map(MM::deserialize).map(this::nonItalic).collect(Collectors.toList());
                meta.lore(loreComp);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private Component nonItalic(Component component) {
        if (component instanceof TextComponent textComponent) {
            TextComponent.Builder builder = Component.text()
                .content(textComponent.content())
                .style(textComponent.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            for (Component child : textComponent.children()) {
                builder.append(nonItalic(child));
            }
            return builder.build();
        }
        return component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private Material parseMaterial(String raw) {
        try {
            return Material.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return Material.STONE;
        }
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, Math.min(54, size));
        int rem = normalized % 9;
        return rem == 0 ? normalized : normalized + (9 - rem);
    }

    private int toInt(Object o, int def) {
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return def;
        }
    }

    private String getString(Map<?, ?> map, String key, String def) {
        Object value = map.get(key);
        if (value == null) return def;
        return String.valueOf(value);
    }

    private List<String> toStringList(Object raw) {
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
