package Model.POJO;

public class CategoryThreshold {
    private int categoryId;
    private int threshold;

    public CategoryThreshold() {}

    public CategoryThreshold(int categoryId, int threshold) {
        this.categoryId = categoryId;
        this.threshold = threshold;
    }

    // Getters and setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}