package run.tere.lib.inventorymanager.builders;

import run.tere.lib.inventorymanager.models.BuildPaginationItem;
import run.tere.lib.inventorymanager.models.Fetch;
import run.tere.lib.inventorymanager.models.Pagination;

public class PaginationBuilder<T> {

    private String id;
    private Fetch<T> fetch;
    private BuildPaginationItem<T> buildPaginationItem;

    public PaginationBuilder(String id) {
        this.id = id;
    }

    public PaginationBuilder<T> fetch(Fetch<T> fetch) {
        this.fetch = fetch;
        return this;
    }

    public PaginationBuilder<T> items(BuildPaginationItem<T> buildPaginationItem) {
        this.buildPaginationItem = buildPaginationItem;
        return this;
    }

    public Pagination<T> build() {
        return new Pagination<T>(id, fetch, buildPaginationItem);
    }

}
