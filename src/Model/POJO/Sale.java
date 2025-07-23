package Model.POJO;

import java.time.LocalDateTime;

public class Sale {
    private int saleId;
    private LocalDateTime saleDate;
    private int saleQty;
    private double totalAmount;

    public Sale() {}

    public Sale(int saleId, LocalDateTime saleDate, int saleQty, double totalAmount) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.saleQty = saleQty;
        this.totalAmount = totalAmount;
    }

    // Getters and setters
    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public int getSaleQty() {
        return saleQty;
    }

    public void setSaleQty(int saleQty) {
        this.saleQty = saleQty;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return String.format("Sale{saleId=%d, saleDate=%s, saleQty=%d, totalAmount=%.2f}",
                saleId, saleDate, saleQty, totalAmount);
    }
}