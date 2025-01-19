package run.tere.lib.inventorymanager.managers;

import org.bukkit.plugin.Plugin;
import run.tere.lib.inventorymanager.listeners.InventoryManagerListener;

public class InnerPluginInventoryManager {

    private Plugin plugin;
    private PluginInventoryManager pluginInventoryManager;

    public InnerPluginInventoryManager(Plugin plugin) {
        this.plugin = plugin;
        this.pluginInventoryManager = new PluginInventoryManager(plugin);
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(new InventoryManagerListener(this), plugin);
    }

    public PluginInventoryManager getPluginInventoryManager() {
        return pluginInventoryManager;
    }

}
