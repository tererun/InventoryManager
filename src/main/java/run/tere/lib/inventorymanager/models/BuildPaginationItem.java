package run.tere.lib.inventorymanager.models;

/**
 * ページネーションアイテムを構築するためのインターフェース
 * @param <T> ページネーションで使用するデータの型
 */
public interface BuildPaginationItem<T> {

    /**
     * ページネーションアイテムを構築します
     * @param t ページネーションで使用するデータ
     * @param page 現在のページ番号（0から始まる）
     * @param index 現在のページ内でのインデックス（0から始まる）
     * @return 構築されたアイテムと次のアイテムがあるかどうか
     */
    PaginationItemResult<T> build(T t, int page, int index);

}
