package Model.POJO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Sale {
    private int id;
    private LocalDateTime dateTime;
    private LocalDate saleDate;
    private int saleQty;
    private double totalAmount;

    public Sale() {}

    public Sale(int id, LocalDateTime dateTime) {
        this.id = id;
        this.dateTime = dateTime;
    }

    public Sale(LocalDate saleDate, double totalAmount) {
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
    }


    public Sale(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public int getSaleQty() {
        return saleQty;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public void setSaleQty(int saleQty) {
        this.saleQty = saleQty;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return String.format("Sale{id=%d, dateTime=%s, qty=%d, total=%.2f}", id, dateTime, saleQty, totalAmount);
    }
}

