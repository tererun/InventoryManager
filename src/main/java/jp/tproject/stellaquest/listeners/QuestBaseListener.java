package jp.tproject.stellaquest.listeners;

import jp.tproject.stellaquest.StellaQuest;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import run.tere.lib.inventorymanager.builders.CustomInventoryBuilder;
import run.tere.lib.inventorymanager.builders.PaginationBuilder;
import run.tere.lib.inventorymanager.managers.PluginInventoryManager;
import run.tere.lib.inventorymanager.models.*;

import java.util.Arrays;
import java.util.List;

public class QuestBaseListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();

        PluginInventoryManager pluginInventoryManager = StellaQuest.getInstance().getInventoryManager();

        BuildPaginationItem<PlayerCollection> collectionItemBuilder = (collection, page, index) -> {
            System.out.println("BuildPaginationItem called: page=" + page + ", index=" + index);
            int actualIndex = page * 9 + index;

            if (collection == null) {
                System.out.println("Collection is null");
                return new PaginationItemResult<>(null, false);
            }
            
            if (collection.getItems() == null) {
                System.out.println("Collection items is null");
                return new PaginationItemResult<>(null, false);
            }
            
            if (actualIndex >= collection.getItems().size()) {
                System.out.println("Index out of bounds: " + actualIndex + " >= " + collection.getItems().size());
                return new PaginationItemResult<>(null, false);
            }

            CollectionItem item = collection.getItems().get(actualIndex);
            System.out.println("Building item: " + item.getName() + " at index " + actualIndex);

            CustomItem<PlayerCollection> customItem = new CustomClickItem<>(' ',
                    (data) -> {
                        ItemStack itemStack = new ItemStack(item.getMaterial());
                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName("§f" + item.getName());
                        itemStack.setItemMeta(meta);
                        return itemStack;
                    },
                    (clickEvent, itemStack, data) -> {
                        Player player1 = (Player) clickEvent.getWhoClicked();
                        player1.sendMessage("§aコレクションアイテム「" + item.getName() + "」を選択しました！");
                    }
            );

            // 次のアイテムがあるかどうかを判定
            boolean hasNext = actualIndex + 1 < collection.getItems().size();
            System.out.println("Has next item: " + hasNext + " (actualIndex=" + actualIndex + ", size=" + collection.getItems().size() + ")");

            return new PaginationItemResult<>(customItem, hasNext);
        };

        // ページネーション作成
        Pagination<PlayerCollection> collectionPagination = new PaginationBuilder<PlayerCollection>("collection")
                .fetch(() -> {
                    System.out.println("Pagination fetch called");
                    return new PlayerCollection();
                })
                .items(collectionItemBuilder)
                .build();

        // インベントリ作成
        CustomInventory<PlayerCollection> collectionInventory = new CustomInventoryBuilder<PlayerCollection>(pluginInventoryManager)
                .setTitle("§6コレクション")
                .setSize(27)
                .fetch(() -> {
                    System.out.println("Main inventory fetch called");
                    return new PlayerCollection();
                })
                .setLayout(
                        "xxxxxxxxx",
                        "collection:Items",
                        "collection:Menu"
                )
                .addPlaceHolder(new CustomClickItem<PlayerCollection>('x', (data) -> {
                    ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName(" ");
                    itemStack.setItemMeta(meta);
                    return itemStack;
                }, (clickEvent, itemStack, data) -> {
                    clickEvent.setCancelled(true);
                }))
                .register(collectionPagination)
                .build();

        // インベントリを開く
        player.openInventory(collectionInventory.getInventory());
        System.out.println("Opened inventory for player: " + player.getName());
    }

    // データクラス
    public class PlayerCollection {
        private List<CollectionItem> items;

        public PlayerCollection() {
            this.items = Arrays.asList(
                    new CollectionItem("アイテム1", Material.DIAMOND),
                    new CollectionItem("アイテム2", Material.GOLD_INGOT),
                    new CollectionItem("アイテム3", Material.IRON_INGOT),
                    new CollectionItem("アイテム4", Material.EMERALD),
                    new CollectionItem("アイテム5", Material.COAL),
                    new CollectionItem("アイテム6", Material.REDSTONE),
                    new CollectionItem("アイテム7", Material.LAPIS_LAZULI),
                    new CollectionItem("アイテム8", Material.NETHERITE_INGOT),
                    new CollectionItem("アイテム9", Material.QUARTZ),
                    new CollectionItem("アイテム10", Material.GLOWSTONE_DUST),
                    new CollectionItem("アイテム11", Material.PRISMARINE_SHARD),
                    new CollectionItem("アイテム12", Material.PRISMARINE_CRYSTALS)
            );
            System.out.println("PlayerCollection created with " + items.size() + " items");
        }

        public List<CollectionItem> getItems() {
            return items;
        }
    }

    // アイテムクラス
    public static class CollectionItem {
        private String name;
        private Material material;

        public CollectionItem(String name, Material material) {
            this.name = name;
            this.material = material;
        }

        public String getName() {
            return name;
        }

        public Material getMaterial() {
            return material;
        }
    }
} 