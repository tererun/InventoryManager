package run.tere.lib.inventorymanager.models;

public interface BuildPaginationItem<T> {

    BuildPaginationItemResult<T> build(T t, int page, int index);

}
