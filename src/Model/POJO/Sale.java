package Model.POJO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sale transaction containing summary details
 * such as the total quantity, amount, timestamp, and its sale items.
 */
public class Sale {
    private int saleId;
    private LocalDateTime saleDate;
    private int saleQty;
    private double totalAmount;
    private List<SaleItem> saleItems = new ArrayList<>();

    public Sale() {}

    public Sale(int saleId, LocalDateTime saleDate, int saleQty, double totalAmount) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.saleQty = saleQty;
        this.totalAmount = totalAmount;
    }

    public Sale(LocalDateTime saleDate, int saleQty, double totalAmount) {
        this.saleDate = saleDate;
        this.saleQty = saleQty;
        this.totalAmount = totalAmount;
    }

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

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }

    @Override
    public String toString() {
        return String.format(
                "Sale{saleId=%d, saleDate=%s, saleQty=%d, totalAmount=%.2f}",
                saleId, saleDate, saleQty, totalAmount
        );
    }
}