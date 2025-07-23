package Model.POJO;

import java.time.LocalDate;

public class SaleItem {
    private int siId;
    private int saleId;
    private int productId;
    private int siQty;
    private double siPrice;
    private LocalDate siDate; // ✅ Use consistent type
    private String productName;
    private String categoryName;

    public SaleItem() {}

    public SaleItem(int saleId, int productId, int siQty, double siPrice, LocalDate siDate) {
        this.saleId = saleId;
        this.productId = productId;
        this.siQty = siQty;
        this.siPrice = siPrice;
        this.siDate = siDate;
    }

    public int getSiId() {
        return siId;
    }

    public void setSiId(int siId) {
        this.siId = siId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public LocalDate getSiDate() {
        return siDate;
    }

    public void setSiDate(LocalDate siDate) {
        this.siDate = siDate;
    }

    public int getSiQty() {
        return siQty;
    }

    public void setSiQty(int siQty) {
        this.siQty = siQty;
    }

    public double getSiPrice() {
        return siPrice;
    }

    public void setSiPrice(double siPrice) {
        this.siPrice = siPrice;
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
}