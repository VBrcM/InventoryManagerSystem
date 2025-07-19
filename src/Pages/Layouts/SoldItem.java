package Pages.Layouts;

import javafx.beans.property.*;

public class SoldItem {

    // Section: Properties
    private final StringProperty name;
    private final StringProperty category;
    private final IntegerProperty quantity;
    private final IntegerProperty price;

    // Section: Constructor
    public SoldItem(String name, String category, int quantity, int price) {
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleIntegerProperty(price);
    }

    // Section: Property getters
    public StringProperty nameProperty() { return name; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty quantityProperty() { return quantity; }
    public IntegerProperty priceProperty() { return price; }

    // Section: Value getters
    public int getQuantity() { return quantity.get(); }
    public int getPrice() { return price.get(); }
}
