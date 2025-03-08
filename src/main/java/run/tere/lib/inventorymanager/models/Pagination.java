package run.tere.lib.inventorymanager.models;

public class Pagination<T> {

    private final String id;
    private final Fetch<T> fetch;
    private final BuildPaginationItem<T> buildPaginationItem;

    public Pagination(String id, Fetch<T> fetch, BuildPaginationItem<T> buildPaginationItem) {
        this.id = id;
        this.fetch = fetch;
        this.buildPaginationItem = buildPaginationItem;
    }

    public String getId() {
        return id;
    }

    public Fetch<T> getFetch() {
        return fetch;
    }

    public BuildPaginationItem<T> getBuildPaginationItem() {
        return buildPaginationItem;
    }

    public String items() {
        return id + ":Items";
    }

    public String menu() {
        return id + ":Menu";
    }

}
