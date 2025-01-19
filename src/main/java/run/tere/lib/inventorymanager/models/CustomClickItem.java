package run.tere.lib.inventorymanager.models;

public class CustomClickItem<T> extends CustomItem<T> {

    private final ClickEvent<T> clickEvent;

    public CustomClickItem(char placeHolder, BuildCustomItem<T> buildCustomItem, ClickEvent<T> clickEvent) {
        super(placeHolder, buildCustomItem);
        this.clickEvent = clickEvent;
    }

    public ClickEvent<T> getClickEvent() {
        return clickEvent;
    }

}
