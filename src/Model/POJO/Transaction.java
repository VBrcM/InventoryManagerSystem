package Model.POJO;

import java.time.LocalDate;

/**
 * Plain Old Java Object (POJO) representing a transaction record.
 */
public class Transaction {
    private int id;
    private int productId;
    private String productName; // Optional display field from JOIN with product
    private int quantity;
    private String type;
    private LocalDate date;

    // ========================
    // Getters and Setters
    // ========================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // ========================
    // Optional: toString()
    // ========================
    @Override
    public String toString() {
        return String.format("Transaction[id=%d, productId=%d, name=%s, qty=%d, type=%s, date=%s]",
                id, productId, productName, quantity, type, date);
    }
}