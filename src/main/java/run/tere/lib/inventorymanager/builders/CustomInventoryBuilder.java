package run.tere.lib.inventorymanager.builders;

import org.checkerframework.checker.units.qual.C;
import run.tere.lib.inventorymanager.managers.PluginInventoryManager;
import run.tere.lib.inventorymanager.models.ClickEvent;
import run.tere.lib.inventorymanager.models.CustomInventory;
import run.tere.lib.inventorymanager.models.CustomItem;
import run.tere.lib.inventorymanager.models.Fetch;

import java.util.HashMap;
import java.util.List;

public class CustomInventoryBuilder<T> {

    private PluginInventoryManager pluginInventoryManager;
    private String title;
    private int size;
    private Fetch<T> fetch;
    private ClickEvent<T> clickEvent;
    private HashMap<Character, CustomItem<T>> customItems;
    private List<String> layout;
    private Class<T> type;

    public CustomInventoryBuilder(PluginInventoryManager pluginInventoryManager, Class<T> type) {
        this.pluginInventoryManager = pluginInventoryManager;
        this.title = "";
        this.size = 54;
        this.fetch = null;
        this.clickEvent = null;
        this.customItems = new HashMap<>();
        this.layout = null;
        this.type = type;
    }

    public CustomInventoryBuilder<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomInventoryBuilder<T> setSize(int size) {
        this.size = size;
        return this;
    }

    public CustomInventoryBuilder<T> addPlaceHolder(Character character, CustomItem<T> customItem) {
        this.customItems.put(character, customItem);
        return this;
    }

    public CustomInventoryBuilder<T> setLayout(String... layout) {
        this.layout = List.of(layout);
        return this;
    }

    public CustomInventoryBuilder<T> fetch(Fetch<T> fetch) {
        this.fetch = fetch;
        return this;
    }

    public CustomInventoryBuilder<T> onClickRaw(ClickEvent<T> clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public CustomInventory<T> build() {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Size must be a multiple of 9.");
        }
        if (size > 54) {
            throw new IllegalArgumentException("Size must be less than or equal to 54.");
        }
        if (size < 9) {
            throw new IllegalArgumentException("Size must be greater than or equal to 9.");
        }
        if (layout != null && layout.size() != size / 9) {
            throw new IllegalArgumentException("Layout size must be equal to size / 9.");
        }

        return new CustomInventory<>(pluginInventoryManager.getPlugin(), title, size, fetch, clickEvent, customItems, layout);
    }

}
