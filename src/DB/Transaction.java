package DB;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Transaction {
    private int transId;
    private int productId;
    private String productName;
    private String categoryName;
    private int quantity;
    private String type;
    private Timestamp transDate;
    private LocalDate date;

    // Constructors
    public Transaction() {}

    public Transaction(int transId, int productId, int quantity, String type, Timestamp transDate) {
        this.transId = transId;
        this.productId = productId;
        this.quantity = quantity;
        this.type = type;
        this.transDate = transDate;
    }

    // Getters and Setters
    public int getTransId() {
        return transId;
    }

    public void setTransId(int transId) {
        this.transId = transId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getTransDate() {
        return transDate;
    }

    public void setTransDate(Timestamp transDate) {
        this.transDate = transDate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return type + " " + quantity + " units of " +
                (productName != null ? productName : "Product ID: " + productId);
    }
}
