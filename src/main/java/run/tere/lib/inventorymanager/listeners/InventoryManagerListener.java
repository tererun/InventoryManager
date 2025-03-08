package run.tere.lib.inventorymanager.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import run.tere.lib.inventorymanager.enums.CustomItemType;
import run.tere.lib.inventorymanager.managers.InnerPluginInventoryManager;
import run.tere.lib.inventorymanager.managers.PluginInventoryManager;
import run.tere.lib.inventorymanager.models.ClickEvent;
import run.tere.lib.inventorymanager.models.CustomClickItem;
import run.tere.lib.inventorymanager.models.CustomInventory;
import run.tere.lib.inventorymanager.models.CustomItem;

import java.util.UUID;

public class InventoryManagerListener implements Listener {

    private final PluginInventoryManager inventoryManager;

    public InventoryManagerListener(InnerPluginInventoryManager innerManager) {
        this.inventoryManager = innerManager.getPluginInventoryManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inventory = e.getView().getTopInventory();
        Player player = (Player) e.getWhoClicked();
        ItemStack itemStack = e.getCurrentItem();
        UUID uuid = player.getUniqueId();

        if (!inventoryManager.getPlayerCustomInventories().containsKey(uuid)) {
            return;
        }
        CustomInventory<?> customInventory = inventoryManager.getPlayerCustomInventories().get(uuid);
        if (customInventory.getInventory() != inventory) {
            return;
        }

        e.setCancelled(true);

        onClickRaw(e, customInventory);
        if (itemStack == null) {
            return;
        }
        
        if (itemStack.getItemMeta() == null) {
            return;
        }
        
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        NamespacedKey typeKey = new NamespacedKey(inventoryManager.getPlugin(), "inventory_manager_custom_type");
        if (!container.has(typeKey, PersistentDataType.STRING)) {
            return;
        }
        
        String customType = container.get(typeKey, PersistentDataType.STRING);
        
        if (customType.equalsIgnoreCase(CustomItemType.NORMAL.toString())) {
            NamespacedKey itemKey = new NamespacedKey(inventoryManager.getPlugin(), "inventory_manager_custom_item");
            if (container.has(itemKey, PersistentDataType.STRING)) {
                String itemTag = container.get(itemKey, PersistentDataType.STRING);
                for (CustomItem<?> customItem : customInventory.getCustomItems()) {
                    if (!customItem.getUUID().toString().equals(itemTag) || !(customItem instanceof CustomClickItem<?> customClickItem)) continue;
                    onItemClick(e, customClickItem, customInventory);
                }
            }
        } else if (customType.equalsIgnoreCase(CustomItemType.PAGINATION.toString())) {
            NamespacedKey itemKey = new NamespacedKey(inventoryManager.getPlugin(), "inventory_manager_custom_item");
            if (container.has(itemKey, PersistentDataType.STRING)) {
                String itemTag = container.get(itemKey, PersistentDataType.STRING);
                UUID paginationUUID = UUID.fromString(itemTag);
                CustomItem<?> paginationItem = customInventory.getPaginationCustomItems().get(paginationUUID);
                if (paginationItem == null) {
                    return;
                }
                if (!(paginationItem instanceof CustomClickItem<?> customClickItem)) {
                    return;
                }
                onItemClick(e, customClickItem, customInventory);
            }
        }
    }

    public <T> void onItemClick(InventoryClickEvent e, CustomClickItem<?> clickItem, CustomInventory<?> inventory) {
        CustomClickItem<T> customClickItem = ((CustomClickItem<T>) clickItem);
        CustomInventory<T> customInventory = ((CustomInventory<T>) inventory);
        customClickItem.getClickEvent().click(e, e.getCurrentItem(), customInventory.getLastFetchResult());
    }

    public <T> void onClickRaw(InventoryClickEvent e, CustomInventory<?> inventory) {
        CustomInventory<T> customInventory = ((CustomInventory<T>) inventory);
        ClickEvent<T> clickEvent = customInventory.getOnClickRaw();
        if (clickEvent == null) return;
        clickEvent.click(e, e.getCurrentItem(), customInventory.getLastFetchResult());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        Player player = (Player) e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!inventoryManager.getPlayerCustomInventories().containsKey(uuid)) return;
        CustomInventory<?> customInventory = inventoryManager.getPlayerCustomInventories().get(uuid);
        if (!customInventory.getInventory().equals(inventory)) return;

        inventoryManager.getPlayerCustomInventories().remove(uuid);
    }

}
