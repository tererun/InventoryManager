package run.tere.lib.inventorymanager.models;

/**
 * インベントリのページネーションを管理するクラス
 * @param <T> ページネーションで使用するデータの型
 */
public class Pagination<T> {

    private final String id;
    private final Fetch<T> fetch;
    private final BuildPaginationItem<T> buildPaginationItem;

    /**
     * ページネーションを作成します
     * @param id ページネーションのID（レイアウトで使用される識別子）
     * @param fetch ページネーションのデータを取得するためのFetch
     * @param buildPaginationItem ページネーションアイテムを構築するためのビルダー
     */
    public Pagination(String id, Fetch<T> fetch, BuildPaginationItem<T> buildPaginationItem) {
        this.id = id;
        this.fetch = fetch;
        this.buildPaginationItem = buildPaginationItem;
    }

    /**
     * ページネーションのIDを取得します
     * @return ページネーションのID
     */
    public String getId() {
        return id;
    }

    /**
     * ページネーションのデータを取得するためのFetchを取得します
     * @return ページネーションのFetch
     */
    public Fetch<T> getFetch() {
        return fetch;
    }

    /**
     * ページネーションアイテムを構築するためのビルダーを取得します
     * @return ページネーションアイテムのビルダー
     */
    public BuildPaginationItem<T> getBuildPaginationItem() {
        return buildPaginationItem;
    }

    /**
     * ページネーションアイテムのレイアウト識別子を取得します
     * @return "ID:Items"形式の識別子
     */
    public String items() {
        return id + ":Items";
    }

    /**
     * ページネーションメニューのレイアウト識別子を取得します
     * @return "ID:Menu"形式の識別子
     */
    public String menu() {
        return id + ":Menu";
    }

}
