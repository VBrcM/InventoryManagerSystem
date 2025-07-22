package Pages.Layouts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class EmployeeInventoryLayout {

    // ========================
    // ========== UI ==========
    // ========================

    public static StackPane build() {
        Label title = new Label("Inventory");
        title.setId("title-label");

        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getStyleClass().add("table-view");

        TableColumn<Product, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("product"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setStyle("-fx-alignment: CENTER;"); // Center align price

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // =============================
        // ========== DATA =============
        // =============================

        ObservableList<Product> products = FXCollections.observableArrayList(
                new Product("Pen", "Stationery", 5, 10.0),
                new Product("Notebook", "Stationery", 25, 30.0),
                new Product("Stapler", "Tools", 7, 50.0),
                new Product("Marker", "Stationery", 2, 15.0),
                new Product("Tape", "Tools", 18, 20.0)
        );

        FilteredList<Product> filteredList = new FilteredList<>(products, p -> true);
        table.setItems(filteredList);

        // ===============================
        // ========== FILTER =============
        // ===============================

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredList.setPredicate(p -> {
                String productName = p.getProduct() != null ? p.getProduct().toLowerCase() : "";
                String categoryName = p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "";
                return productName.contains(lower) || categoryName.contains(lower);
            });
        });

        // =====================================
        // ========== LOW STOCK STYLE ==========
        // =====================================

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                getStyleClass().remove("low-stock");
                setTooltip(null);

                if (product != null && !empty) {
                    if (product.getStock() < 10) {
                        getStyleClass().add("low-stock");
                        setTooltip(new Tooltip("Low stock! Consider restocking soon."));
                    }
                }
            }
        });

        VBox content = new VBox(20, title, searchField, table);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("content-area");

        return new StackPane(content);
    }

    // ===================================
    // ========== PRODUCT CLASS ==========
    // ===================================

    public static class Product {
        private final String product;
        private final String categoryName;
        private final int stock;
        private final double price;

        public Product(String product, String categoryName, int stock, double price) {
            this.product = product;
            this.categoryName = categoryName;
            this.stock = stock;
            this.price = price;
        }

        public String getProduct() {
            return product;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public int getStock() {
            return stock;
        }

        public double getPrice() {
            return price;
        }
    }
}
