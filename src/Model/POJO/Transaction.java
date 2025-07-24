package Model.POJO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {


    // Common fields
    private int tId;
    private LocalDateTime tDate;

    // Cart-level fields
    private int saleId;
    private int tQty;
    private double amount;

    // Item-level fields (used only when viewing or logging per-product)
    private int productId;
    private String productName;
    private String description;
    private double price;
    private Product product;
    private String time;
    private String itemsSummary;
    private double total;

    // === Constructors ===
    //Default
    public Transaction() {}

    // Used when inserting full cart-based transaction (one per cart)
    public Transaction(int saleId, int tQty, double amount, LocalDateTime tDate) {
        this.saleId = saleId;
        this.tQty = tQty;
        this.amount = amount;
        this.tDate = tDate;
    }

    // Used when retrieving/viewing item-level transaction info (joined from DB)
    public Transaction(int tId, int saleId, int productId, String productName, int tQty, double amount, String description, LocalDateTime tDate) {
        this.tId = tId;
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.tQty = tQty;
        this.amount = amount;
        this.description = description;
        this.tDate = tDate;
    }

    // Optional: Minimal constructor for logging simple item movement (e.g. old per-item logic)
    public Transaction(int productId, int qty, LocalDateTime tDate) {
        this.productId = productId;
        this.tQty = qty;
        this.tDate = tDate;
    }


    // Employee Transaction Layout
    public Transaction(String time, String itemsSummary, double total) {
        this.time = time;
        this.itemsSummary = itemsSummary;
        this.total = total;
    }

    public String getTime() {
        return time;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }

    public double getTotal() {
        return total;
    }
//=====
    // === Getters and Setters ===
    public int getTId() {
        return tId;
    }

    public void setTId(int tId) {
        this.tId = tId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getTQty() {
        return tQty;
    }

    public void setTQty(int tQty) {
        this.tQty = tQty;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTDate() {
        return tDate;
    }

    public void setTDate(LocalDateTime tDate) {
        this.tDate = tDate;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName != null ? productName : "";
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedTime() {
        if (tDate == null) return "";
        return tDate.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
