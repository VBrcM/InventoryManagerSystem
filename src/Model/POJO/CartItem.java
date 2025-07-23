package Model.POJO;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CartItem {
    private final Product product;
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity.set(quantity);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getTotal() {
        return product.getProductPrice() * quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public double getSubtotal() {
        return product.getProductPrice() * quantity.get();
    }

}