package Model.POJO;

public class CategoryThreshold {
    private int categoryId;
    private String categoryName;
    private int threshold;

    public CategoryThreshold() {}

    public CategoryThreshold(int categoryId, String categoryName, int threshold) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.threshold = threshold;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}