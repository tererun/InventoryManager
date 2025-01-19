package run.tere.lib.inventorymanager.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import run.tere.lib.inventorymanager.models.CustomInventory;

import java.util.HashMap;
import java.util.UUID;

public class PluginInventoryManager {

    private Plugin plugin;
    private HashMap<UUID, CustomInventory<?>> playerCustomInventories;

    public PluginInventoryManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerCustomInventories = new HashMap<>();
    }

    public HashMap<UUID, CustomInventory<?>> getPlayerCustomInventories() {
        return playerCustomInventories;
    }

    public void openInventory(Player player, CustomInventory<?> customInventory) {
        player.openInventory(customInventory.getInventory());
        playerCustomInventories.put(player.getUniqueId(), customInventory);
    }

    public Plugin getPlugin() {
        return plugin;
    }

}
