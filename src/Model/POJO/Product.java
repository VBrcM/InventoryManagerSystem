package Model.POJO;

public class Product {
    private int productId;
    private int categoryId;
    private String productName;
    private String description;
    private double productPrice;
    private int stock;
    private String categoryName;

    public Product() {}

    //convenience
    public Product(int categoryId, String productName, String description,
                   double productPrice, int stock) {
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.productPrice = productPrice;
        this.stock = stock;
    }

    public Product(int productId, int categoryId, String productName,
                   String description, double productPrice, int stock) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.productPrice = productPrice;
        this.stock = stock;
    }

    // Getters and setters
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

    public String getProductName() {
        return productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    }

    @Override
    public String toString() {
        return productName;
    }
}