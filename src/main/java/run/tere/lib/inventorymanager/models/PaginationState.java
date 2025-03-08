package run.tere.lib.inventorymanager.models;

public class PaginationState {

    private final String id;
    private int currentPage;
    private boolean isLastPage;

    public PaginationState(String id, int currentPage, boolean isLastPage) {
        this.id = id;
        this.currentPage = currentPage;
        this.isLastPage = isLastPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage < 0) return;
        this.currentPage = currentPage;
    }

    public String getId() {
        return id;
    }

}
