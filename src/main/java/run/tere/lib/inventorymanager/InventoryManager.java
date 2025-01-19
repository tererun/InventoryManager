package run.tere.lib.inventorymanager;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import run.tere.lib.inventorymanager.managers.InnerPluginInventoryManager;
import run.tere.lib.inventorymanager.managers.PluginInventoryManager;

import java.util.HashMap;

public final class InventoryManager extends JavaPlugin {

    private static HashMap<Plugin, InnerPluginInventoryManager> pluginInventoryManagerHashMap = new HashMap<>();

    public static HashMap<Plugin, InnerPluginInventoryManager> getPluginInventoryManagerHashMap() {
        return pluginInventoryManagerHashMap;
    }

    public static PluginInventoryManager register(Plugin plugin) {
        if (pluginInventoryManagerHashMap.containsKey(plugin)) {
            throw new IllegalArgumentException("PluginInventoryManager is already registered.");
        }

        InnerPluginInventoryManager innerPluginInventoryManager = new InnerPluginInventoryManager(plugin);
        pluginInventoryManagerHashMap.put(plugin, innerPluginInventoryManager);
        innerPluginInventoryManager.registerEvents();

        return innerPluginInventoryManager.getPluginInventoryManager();
    }

}
