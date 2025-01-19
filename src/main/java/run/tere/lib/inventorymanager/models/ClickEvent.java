package run.tere.lib.inventorymanager.models;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface ClickEvent<T> {

    void click(InventoryClickEvent e, ItemStack itemStack, T t);

}
