package Model.POJO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private int id;
    private int productId;
    private String productName; // from JOIN
    private int quantity;
    private String type;
    private LocalDate date;

    // New Fields (add this)
    private LocalDateTime timestamp;
    private double amount;
    private String description;
    private String formattedTime;

    // ========================
    // Getters and Setters
    // ========================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    // === New Getters/Setters ===
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // === Formatted Time ===
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    public String getFormattedTime() {
        if (formattedTime != null) return formattedTime;
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    @Override
    public String toString() {
        return String.format("Transaction[id=%d, productId=%d, name=%s, qty=%d, type=%s, date=%s, amount=%.2f]",
                id, productId, productName, quantity, type, date, amount);
    }
}
