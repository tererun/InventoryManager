package run.tere.lib.inventorymanager.models;

/**
 * ページネーションアイテムの構築結果を表すクラス
 * @param <T> ページネーションで使用するデータの型
 */
public class PaginationItemResult<T> {

    private final CustomItem<T> item;
    private final boolean hasNext;

    /**
     * ページネーションアイテムの結果を作成します
     * @param item 構築されたアイテム（nullの場合はアイテムなし）
     * @param hasNext 次のアイテムがあるかどうか
     */
    public PaginationItemResult(CustomItem<T> item, boolean hasNext) {
        this.item = item;
        this.hasNext = hasNext;
    }

    /**
     * 構築されたアイテムを取得します
     * @return アイテム
     */
    public CustomItem<T> getItem() {
        return item;
    }

    /**
     * 次のアイテムがあるかどうかを取得します
     * @return 次のアイテムがある場合はtrue
     */
    public boolean hasNext() {
        return hasNext;
    }
} 