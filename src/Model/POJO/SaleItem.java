package Model.POJO;

import java.time.LocalDateTime;

/**
 * Represents a single item sold in a sale transaction.
 * Includes product information, quantity, price, and timestamp.
 */
public class SaleItem {
    private int siId;
    private int saleId;
    private int productId;
    private int siQty;
    private double siPrice;
    private LocalDateTime siDate;
    private String productName;
    private String categoryName;
    private Product product;

    public SaleItem() {}

    public SaleItem(int siId, int saleId, int productId,
                    int siQty, double siPrice, LocalDateTime siDate) {
        this.siId = siId;
        this.saleId = saleId;
        this.productId = productId;
        this.siQty = siQty;
        this.siPrice = siPrice;
        this.siDate = siDate;
    }

    public String getProductName() {
        return product != null ? product.getProductName() : productName;
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

    public LocalDateTime getSiDate() {
        return siDate;
    }

    public void setSiDate(LocalDateTime siDate) {
        this.siDate = siDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}