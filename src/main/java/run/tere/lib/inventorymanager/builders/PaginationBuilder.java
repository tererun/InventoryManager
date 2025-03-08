package run.tere.lib.inventorymanager.builders;

import run.tere.lib.inventorymanager.models.BuildPaginationItem;
import run.tere.lib.inventorymanager.models.Fetch;
import run.tere.lib.inventorymanager.models.Pagination;

/**
 * ページネーションを構築するためのビルダークラス
 * @param <T> ページネーションで使用するデータの型
 */
public class PaginationBuilder<T> {

    private String id;
    private Fetch<T> fetch;
    private BuildPaginationItem<T> buildPaginationItem;

    /**
     * ページネーションビルダーを作成します
     * @param id ページネーションのID
     */
    public PaginationBuilder(String id) {
        this.id = id;
    }

    /**
     * データ取得用のFetchを設定します
     * @param fetch データ取得用のFetch
     * @return このビルダー
     */
    public PaginationBuilder<T> fetch(Fetch<T> fetch) {
        this.fetch = fetch;
        return this;
    }

    /**
     * ページネーションアイテムのビルダーを設定します
     * @param buildPaginationItem ページネーションアイテムのビルダー
     * @return このビルダー
     */
    public PaginationBuilder<T> items(BuildPaginationItem<T> buildPaginationItem) {
        this.buildPaginationItem = buildPaginationItem;
        return this;
    }

    /**
     * ページネーションを構築します
     * @return 構築されたページネーション
     */
    public Pagination<T> build() {
        return new Pagination<T>(id, fetch, buildPaginationItem);
    }

}
