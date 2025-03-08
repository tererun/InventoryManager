package run.tere.lib.inventorymanager.models;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import run.tere.lib.inventorymanager.enums.CustomItemType;

import java.util.UUID;

public class CustomItem<T> {

    private final UUID uuid;
    private final char placeHolder;
    private final BuildCustomItem<T> buildCustomItem;

    public CustomItem(char placeHolder, BuildCustomItem<T> buildCustomItem) {
        this.uuid = UUID.randomUUID();
        this.placeHolder = placeHolder;
        this.buildCustomItem = buildCustomItem;
    }

    public ItemStack build(T t, Plugin plugin, CustomItemType customItemType) {
        ItemStack itemStack = buildCustomItem.build(t);
        if (itemStack == null) return null;

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "inventory_manager_custom_type"), PersistentDataType.STRING, customItemType.toString());
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "inventory_manager_custom_item"), PersistentDataType.STRING, uuid.toString());
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public UUID getUUID() {
        return uuid;
    }

    public char getPlaceHolder() {
        return placeHolder;
    }

    public BuildCustomItem<T> getBuildCustomItem() {
        return buildCustomItem;
    }

}
