package run.tere.lib.inventorymanager.models;

import org.bukkit.inventory.ItemStack;

public interface BuildCustomItem<T> {

    ItemStack build(T t);

}
