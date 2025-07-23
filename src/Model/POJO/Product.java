package Model.POJO;

public class Product {

    private int productId;
    private int categoryId;
    private String categoryName; // Optional, used when joined with category table
    private String productName;
    private String description;
    private double productPrice;
    private int stock;
    private int threshold;
    private boolean lowStock; // Cached value for UI bindings, optional

    public Product() {}

    public Product(int productId, int categoryId, String productName, String description, double productPrice, int stock) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.productPrice = productPrice;
        this.stock = stock;
        this.lowStock = isLowStock(); // initialize cache
    }

    // =====================
    // Getters and Setters
    // =====================

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
        updateLowStock(); // refresh cached value
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
        updateLowStock(); // refresh cached value
    }

    // =====================
    // Low Stock Logic
    // =====================

    public boolean isLowStock() {
        return stock <= threshold;
    }

    public boolean getLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean value) {
        // Optional setter for compatibility, though value is derived
        this.lowStock = value;
    }

    private void updateLowStock() {
        this.lowStock = isLowStock();
    }

    @Override
    public String toString() {
        return productName;
    }
}