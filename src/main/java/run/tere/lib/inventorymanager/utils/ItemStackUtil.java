package run.tere.lib.inventorymanager.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemStackUtil {

    public static ItemStack addCustomTag(ItemStack itemStack, NamespacedKey key, String value) {
        if (itemStack == null) return null;
        itemStack = itemStack.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static String getCustomTag(ItemStack itemStack, NamespacedKey key) {
        if (itemStack == null) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

}
