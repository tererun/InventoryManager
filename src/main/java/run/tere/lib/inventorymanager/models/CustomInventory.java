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
        if (pagination == null || paginationT == null || state == null) {
            System.out.println("buildPaginationItems: pagination=" + pagination + ", paginationT=" + paginationT + ", state=" + state);
            return;
        }
        
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
        
        System.out.println("buildPaginationItems: id=" + id + ", currentPage=" + currentPage + ", itemRowIndex=" + itemRowIndex + ", startIndex=" + startIndex);
        
        // 最初は最後のページと仮定
        state.setLastPage(true);
        
        // アイテムの総数を取得（デバッグ用）
        int totalItems = 0;
        boolean foundItems = false;
        
        for (int j = 0; j < 9; j++) {
            // 実際のインデックスを計算
            int actualIndex = startIndex + j;
            PaginationItemResult<T> result = pagination.getBuildPaginationItem().build(paginationT, currentPage, actualIndex);
            CustomItem<T> customItem = result.getItem();
            if (customItem == null) continue;
            
            foundItems = true;
            totalItems++;
            
            ItemStack itemStack = customItem.build(paginationT, plugin, CustomItemType.PAGINATION);
            inventory.setItem(i * 9 + j, itemStack);
            paginationCustomItems.put(customItem.getUUID(), customItem);
        }
        
        // 次のページがあるかどうかを確認
        // 次のページのアイテムが存在するかテスト
        boolean hasNextPage = false;
        PaginationItemResult<T> testResult = pagination.getBuildPaginationItem().build(paginationT, currentPage + 1, 0);
        if (testResult.getItem() != null) {
            hasNextPage = true;
            state.setLastPage(false);
            System.out.println("Setting isLastPage to false because there are more items in the next page");
        }
        
        System.out.println("After buildPaginationItems: isLastPage=" + state.isLastPage() + ", currentPage=" + currentPage + ", totalItems=" + totalItems + ", hasNextPage=" + hasNextPage);
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
        if (state == null) {
            System.out.println("buildPaginationMenuLine: state is null for id=" + id);
            return;
        }
        
        System.out.println("buildPaginationMenuLine: id=" + id + ", currentPage=" + state.getCurrentPage() + ", isLastPage=" + state.isLastPage());
        
        // クリア
        for (int j = 0; j < 9; j++) {
            inventory.setItem(i * 9 + j, null);
        }
        
        // 戻るボタン
        if (state.getCurrentPage() == 0) {
            System.out.println("Not showing back button because currentPage=0");
            // 最初のページでは戻るボタンを表示しない
        } else {
            System.out.println("Showing back button");
            CustomClickItem<T> customItem = new CustomClickItem<>(' ', (data) -> {
                // 戻るボタン用のアイテム
                ItemStack itemStack = new ItemStack(org.bukkit.Material.ARROW);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f< 戻る");
                itemStack.setItemMeta(meta);
                return itemStack;
            }, (onClickRaw, itemStack, t) -> {
                System.out.println("Back button clicked");
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
            paginationCustomItems.put(customItem.getUUID(), customItem);
            System.out.println("Added back button with UUID: " + customItem.getUUID());
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
        paginationCustomItems.put(pageNumberItem.getUUID(), pageNumberItem);
        System.out.println("Added page number with UUID: " + pageNumberItem.getUUID());
        
        // 次へボタン
        if (state.isLastPage()) {
            System.out.println("Not showing next button because isLastPage=true");
            // 最後のページでは次へボタンを表示しない
        } else {
            System.out.println("Showing next button");
            CustomClickItem<T> customItem = new CustomClickItem<>(' ', (data) -> {
                // 次へボタン用のアイテム
                ItemStack itemStack = new ItemStack(org.bukkit.Material.ARROW);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f次へ >");
                itemStack.setItemMeta(meta);
                return itemStack;
            }, (clickEvent, itemStack, t) -> {
                System.out.println("Next button clicked");
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
            paginationCustomItems.put(customItem.getUUID(), customItem);
            System.out.println("Added next button with UUID: " + customItem.getUUID());
        }
    }

    private void buildPagination(String specificLine, HashMap<String, T> paginationFetch) {
        // メインスレッドでUIを更新
        Bukkit.getScheduler().runTask(plugin, () -> {
            System.out.println("Building pagination for " + specificLine);
            
            // 古いページネーションアイテムをクリア
            System.out.println("Clearing old pagination items: " + paginationCustomItems.size());
            paginationCustomItems.clear();
            
            for (int j = 0; j < layout.size(); j++) {
                String layoutLine = layout.get(j);
                if (layoutLine.equalsIgnoreCase(specificLine + ":Items")) {
                    buildPaginationItems(layoutLine, j, paginationFetch);
                }
            }
            buildPaginationMenu(paginationFetch, specificLine);
            
            System.out.println("Pagination built with " + paginationCustomItems.size() + " items");
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
