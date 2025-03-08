package run.tere.lib.inventorymanager.models;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
        this.paginationCustomItems = new HashMap<>();
        this.layout = layout;

        build();
    }

    public void build() {
        if (fetch == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                T t = fetch.fetch();
                HashMap<String, T> paginationFetch = new HashMap<>();
                for (Pagination<T> pagination : paginations.values()) {
                    try {
                        String id = pagination.getId();
                        T fetchResult = pagination.getFetch().fetch();
                        paginationFetch.put(id, fetchResult);
                        paginationStateMap.put(id, new PaginationState(id, 0, false));
                    } catch (Exception e) {
                        // ページネーションのフェッチでエラーが発生した場合はログに記録
                        plugin.getLogger().warning("Error fetching pagination data for " + pagination.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (t == null) return;
                lastFetchResult = t;
                if (layout == null) return;
                
                // メインスレッドでUIを更新
                Bukkit.getScheduler().runTask(plugin, () -> {
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
            } catch (Exception e) {
                // メインのフェッチでエラーが発生した場合はログに記録
                plugin.getLogger().severe("Error building inventory: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void buildPaginationItems(String layoutLine, int i, HashMap<String, T> paginationFetch) {
        String id = layoutLine.replace(":Items", "");
        Pagination<T> pagination = paginations.get(id);
        T paginationT = paginationFetch.get(id);
        PaginationState state = paginationStateMap.get(id);
        if (pagination == null || paginationT == null || state == null) return;
        
        // 現在のページのアイテムをクリア
        for (int j = 0; j < 9; j++) {
            inventory.setItem(i * 9 + j, null);
        }
        
        // 現在のページに基づいてアイテムを構築
        int currentPage = state.getCurrentPage();
        
        // この行が何番目のItems行かを計算
        int itemRowIndex = 0;
        for (int k = 0; k < i; k++) {
            if (k < layout.size() && layout.get(k).equals(id + ":Items")) {
                itemRowIndex++;
            }
        }
        
        // この行の開始インデックスを計算
        int startIndex = itemRowIndex * 9;
        
        for (int j = 0; j < 9; j++) {
            // 実際のインデックスを計算（ページ * 全行のアイテム数 + この行の開始インデックス + 列インデックス）
            int actualIndex = startIndex + j;
            PaginationItemResult<T> result = pagination.getBuildPaginationItem().build(paginationT, currentPage, actualIndex);
            CustomItem<T> customItem = result.getItem();
            if (customItem == null) continue;
            
            ItemStack itemStack = customItem.build(paginationT, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + j, itemStack);
            paginationCustomItems.put(customItem.getUUID(), customItem);
            
            // 最後の行かつ次のアイテムがある場合、最後のページではない
            if (isLastItemsRow(id, i) && result.hasNext()) {
                state.setLastPage(false);
            }
        }
        
        // 最後の行の場合、最後のページかどうかを設定
        if (isLastItemsRow(id, i)) {
            // デフォルトでは最後のページと仮定
            if (!state.isLastPage()) {
                state.setLastPage(true);
            }
        }
    }
    
    // 指定された行が指定されたIDの最後のItems行かどうかを判定
    private boolean isLastItemsRow(String id, int rowIndex) {
        for (int i = rowIndex + 1; i < layout.size(); i++) {
            if (layout.get(i).equals(id + ":Items")) {
                return false;
            }
        }
        return true;
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
        
        // クリア
        for (int j = 0; j < 9; j++) {
            inventory.setItem(i * 9 + j, null);
        }
        
        // 戻るボタン
        if (state.getCurrentPage() == 0) {
            // 最初のページでは戻るボタンを表示しない
        } else {
            CustomItem<T> customItem = new CustomClickItem<>(' ', (data) -> SkullUtil.createSkull("http://textures.minecraft.net/texture/b76230a0ac52af11e4bc84009c6890a4029472f3947b4f465b5b5722881aacc7", "§f< 戻る"), (onClickRaw, itemStack, t) -> {
                state.setCurrentPage(state.getCurrentPage() - 1);
                // データを再取得してページネーションを更新
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    T paginationT = paginations.get(id).getFetch().fetch();
                    if (paginationT != null) {
                        paginationFetch.put(id, paginationT);
                        buildPagination(id, paginationFetch);
                    }
                });
            });
            ItemStack itemStack = customItem.build(null, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + 2, itemStack);
        }
        
        // ページ番号表示
        CustomItem<T> pageNumberItem = new CustomItem<>(' ', (data) -> {
            ItemStack pageItem = new ItemStack(org.bukkit.Material.PAPER);
            ItemMeta meta = pageItem.getItemMeta();
            meta.setDisplayName("§fページ " + (state.getCurrentPage() + 1));
            pageItem.setItemMeta(meta);
            return pageItem;
        });
        ItemStack pageNumberStack = pageNumberItem.build(null, plugin, CustomItemType.PAGINATION);
        inventory.setItem(i * 9 + 4, pageNumberStack);
        
        // 次へボタン
        if (state.isLastPage()) {
            // 最後のページでは次へボタンを表示しない
        } else {
            CustomItem<T> customItem = new CustomClickItem<>(' ', (data) -> SkullUtil.createSkull("http://textures.minecraft.net/texture/dbf8b6277cd36266283cb5a9e6943953c783e6ff7d6a2d59d15ad0697e91d43c", "§f次へ >"), (clickEvent, itemStack, t) -> {
                state.setCurrentPage(state.getCurrentPage() + 1);
                // データを再取得してページネーションを更新
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    T paginationT = paginations.get(id).getFetch().fetch();
                    if (paginationT != null) {
                        paginationFetch.put(id, paginationT);
                        buildPagination(id, paginationFetch);
                    }
                });
            });
            ItemStack itemStack = customItem.build(null, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + 6, itemStack);
        }
    }

    private void buildPagination(String specificLine, HashMap<String, T> paginationFetch) {
        // メインスレッドでUIを更新
        Bukkit.getScheduler().runTask(plugin, () -> {
            // 古いページネーションアイテムをクリア
            paginationCustomItems.clear();
            
            for (int j = 0; j < layout.size(); j++) {
                String layoutLine = layout.get(j);
                if (layoutLine.equalsIgnoreCase(specificLine + ":Items")) {
                    buildPaginationItems(layoutLine, j, paginationFetch);
                }
            }
            buildPaginationMenu(paginationFetch, specificLine);
        });
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
