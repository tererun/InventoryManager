package run.tere.lib.inventorymanager.models;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import run.tere.lib.inventorymanager.enums.CustomItemType;
import run.tere.lib.inventorymanager.utils.SkullUtil;

import java.util.*;

public class CustomInventory<T> {

    private Plugin plugin;
    private Inventory inventory;

    private Fetch<T> fetch;
    private T lastFetchResult;
    private ClickEvent<T> onClickRaw;
    private HashMap<Character, CustomItem<T>> customItems;
    private HashMap<UUID, CustomItem<T>> paginationCustomItems;
    private HashMap<String, Pagination<T>> paginations;
    private HashMap<String, PaginationState> paginationStateMap;
    private List<String> layout;

    public CustomInventory(
            Plugin plugin,
            String title,
            int size,
            Fetch<T> fetch,
            ClickEvent<T> onClickRaw,
            HashMap<Character, CustomItem<T>> customItems,
            List<String> layout,
            HashMap<String, Pagination<T>> pagination
    ) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.fetch = fetch;
        this.lastFetchResult = null;
        this.onClickRaw = onClickRaw;
        this.customItems = customItems;
        this.paginations = pagination;
        this.paginationStateMap = new HashMap<>();
        this.layout = layout;

        build();
    }

    public void build() {
        if (fetch == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            T t = fetch.fetch();
            HashMap<String, T> paginationFetch = new HashMap<>();
            for (Pagination<T> pagination : paginations.values()) {
                String id = pagination.getId();
                paginationFetch.put(id, pagination.getFetch().fetch());
                paginationStateMap.put(id, new PaginationState(id, 0, false));
            }
            if (t == null) return;
            lastFetchResult = t;
            if (layout == null) return;
            for (int i = 0; i < layout.size(); i++) {
                String layoutLine = layout.get(i);
                if (layoutLine.endsWith(":Items")) {
                    buildPaginationItems(layoutLine, i, paginationFetch);
                    continue;
                } else if (layoutLine.endsWith(":Menu")) {
                    continue;
                }
                for (int j = 0; j < layoutLine.length(); j++) {
                    char c = layoutLine.charAt(j);
                    ItemStack itemStack = null;
                    if (customItems.containsKey(c)) {
                        itemStack = customItems.get(c).build(t, plugin, CustomItemType.NORMAL);
                    }
                    inventory.setItem(i * 9 + j, itemStack);
                }
            }
            buildPaginationMenu(paginationFetch, null);
        });
    }

    private void buildPaginationItems(String layoutLine, int i, HashMap<String, T> paginationFetch) {
        String id = layoutLine.replace(":Items", "");
        Pagination<T> pagination = paginations.get(id);
        T paginationT = paginationFetch.get(id);
        if (pagination == null || paginationT == null) return;
        for (int j = 0; j < 9; j++) {
            BuildPaginationItemResult<T> result = pagination.getBuildPaginationItem().build(paginationT, 0, j);
            CustomItem<T> customItem = result.customItem();
            ItemStack itemStack = customItem.build(paginationT, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + j, itemStack);
            paginationCustomItems.put(customItem.getUUID(), customItem);
            if (!result.hasNext()) {
                paginationStateMap.get(id).setLastPage(true);
                break;
            }
        }
    }

    private void buildPaginationMenu(HashMap<String, T> paginationFetch, String specificLine) {
        if (specificLine == null) {
            for (int i = 0; i < layout.size(); i++) {
                String layoutLine = layout.get(i);
                if (layoutLine.endsWith(":Menu")) {
                    buildPaginationMenuLine(paginationFetch, i, layoutLine);
                }
            }
        } else {
            for (int i = 0; i < layout.size(); i++) {
                String layoutLine = layout.get(i);
                if (layoutLine.equalsIgnoreCase(specificLine + ":Menu")) {
                    buildPaginationMenuLine(paginationFetch, i, layoutLine);
                }
            }
        }
    }

    private void buildPaginationMenuLine(HashMap<String, T> paginationFetch, int i, String layoutLine) {
        String id = layoutLine.replace(":Menu", "");
        PaginationState state = paginationStateMap.get(id);
        if (state == null) return;
        if (state.getCurrentPage() == 0) {
            inventory.setItem(i * 9 + 2, null);
        } else {
            CustomItem<T> customItem = new CustomClickItem<>(' ', (data) -> SkullUtil.createSkull("http://textures.minecraft.net/texture/b76230a0ac52af11e4bc84009c6890a4029472f3947b4f465b5b5722881aacc7", "§f< 戻る"), (onClickRaw, itemStack, t) -> {
                state.setCurrentPage(state.getCurrentPage() - 1);
                buildPagination(id, paginationFetch);
            });
            ItemStack itemStack = customItem.build(null, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + 2, itemStack);
        }
        if (state.isLastPage()) {
            inventory.setItem(i * 9 + 6, null);
        } else {
            CustomItem<T> customItem = new CustomClickItem<>(' ', (data) -> SkullUtil.createSkull("http://textures.minecraft.net/texture/dbf8b6277cd36266283cb5a9e6943953c783e6ff7d6a2d59d15ad0697e91d43c", "§f次へ >"), (clickEvent, itemStack, t) -> {
                state.setCurrentPage(state.getCurrentPage() + 1);
                buildPagination(id, paginationFetch);
            });
            ItemStack itemStack = customItem.build(null, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + 6, itemStack);
        }
    }

    private void buildPagination(String specificLine, HashMap<String, T> paginationFetch) {
        for (int j = 0; j < layout.size(); j++) {
            String layoutLine = layout.get(j);
            if (specificLine.equalsIgnoreCase(layoutLine + ":Items")) {
                buildPaginationItems(layoutLine, j, paginationFetch);
            }
        }
        buildPaginationMenu(paginationFetch, specificLine);
    }

    public T getLastFetchResult() {
        return lastFetchResult;
    }

    public ClickEvent<T> getOnClickRaw() {
        return onClickRaw;
    }

    public HashMap<UUID, CustomItem<T>> getPaginationCustomItems() {
        return paginationCustomItems;
    }

    public Collection<CustomItem<T>> getCustomItems() {
        return customItems.values();
    }

    public Inventory getInventory() {
        return inventory;
    }

}
