package run.tere.lib.inventorymanager.models;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CustomInventory<T> {

    private final Class<T> type;

    private Plugin plugin;
    private Inventory inventory;

    private Fetch<T> fetch;
    private T lastFetchResult;
    private ClickEvent<T> onClickRaw;
    private HashMap<Character, CustomItem<T>> customItems;
    private List<String> layout;

    public CustomInventory(
            Class<T> type,
            Plugin plugin,
            String title,
            int size,
            Fetch<T> fetch,
            ClickEvent<T> onClickRaw,
            HashMap<Character, CustomItem<T>> customItems,
            List<String> layout
    ) {
        this.type = type;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.fetch = fetch;
        this.lastFetchResult = null;
        this.onClickRaw = onClickRaw;
        this.customItems = customItems;
        this.layout = layout;

        build();
    }

    public void build() {
        if (fetch == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            T t = fetch.fetch();
            if (t == null) return;
            lastFetchResult = t;
            if (layout == null) return;
            for (int i = 0; i < layout.size(); i++) {
                String layoutLine = layout.get(i);
                for (int j = 0; j < layoutLine.length(); j++) {
                    char c = layoutLine.charAt(j);
                    ItemStack itemStack = null;
                    if (customItems.containsKey(c)) {
                        itemStack = customItems.get(c).build(t, plugin);
                    }
                    inventory.setItem(i * 9 + j, itemStack);
                }
            }
        });
    }

    public Class<T> getType() {
        return type;
    }

    public T getLastFetchResult() {
        return lastFetchResult;
    }

    public ClickEvent<T> getOnClickRaw() {
        return onClickRaw;
    }

    public Collection<CustomItem<T>> getCustomItems() {
        return customItems.values();
    }

    public Inventory getInventory() {
        return inventory;
    }

}
