package run.tere.lib.inventorymanager.models;

public record BuildPaginationItemResult<T>(CustomItem<T> customItem, boolean hasNext) {
}
