package Pages.Layouts;

import Dialogs.PopUpDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class EmployeeSalesLayout {

    // ============================================
    // ========== PRODUCT & CART MODELS ===========
    // ============================================

    public static class Product {
        private final String name;
        private final double price;
        private final int stock;

        public Product(String name, double price, int stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
    }

    public static class CartItem {
        private final Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getTotal() { return product.getPrice() * quantity; }
    }

    // ========================================
    // ========== MAIN LAYOUT BUILD ===========
    // ========================================

    public static VBox build(BorderPane parentLayout) {
        VBox layout = new VBox();
        layout.setPadding(new Insets(20));
        VBox.setVgrow(layout, Priority.ALWAYS);

        // ---------- Grid Layout ----------
        GridPane contentArea = new GridPane();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setHgap(20);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.ALWAYS);
        contentArea.getRowConstraints().add(rowConstraints);

        ColumnConstraints productsCol = new ColumnConstraints();
        productsCol.setPercentWidth(60);
        productsCol.setHgrow(Priority.ALWAYS);

        ColumnConstraints cartCol = new ColumnConstraints();
        cartCol.setPercentWidth(40);
        cartCol.setHgrow(Priority.ALWAYS);

        contentArea.getColumnConstraints().addAll(productsCol, cartCol);

        // ---------- Products Section ----------
        VBox productsContainer = new VBox(10);
        productsContainer.setAlignment(Pos.TOP_CENTER);
        GridPane.setVgrow(productsContainer, Priority.ALWAYS);
        productsContainer.setMaxHeight(Double.MAX_VALUE);

        Label title = new Label("New Sale");
        title.setId("title-label");

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.getStyleClass().add("input-field");

        TableView<Product> productsTable = createProductsTable();
        VBox.setVgrow(productsTable, Priority.ALWAYS);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("primary-button");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setPrefHeight(40);

        productsContainer.getChildren().addAll(titleBox, searchField, productsTable, addToCartBtn);

        // ---------- Cart Section ----------
        VBox cartContainer = new VBox();
        GridPane.setVgrow(cartContainer, Priority.ALWAYS);
        cartContainer.setMaxHeight(Double.MAX_VALUE);

        VBox cartTableBox = new VBox(10);
        cartTableBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(cartTableBox, Priority.ALWAYS);

        TableView<CartItem> cartTable = createCartTable();
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        HBox cartButtons = new HBox(10);
        cartButtons.setAlignment(Pos.CENTER);
        cartButtons.setPadding(new Insets(10, 0, 0, 0));

        Button clearCartBtn = new Button("🗑Empty Cart");
        clearCartBtn.getStyleClass().add("danger-button");
        clearCartBtn.setPrefHeight(40);

        Button completeSaleBtn = new Button("Complete Sale");
        completeSaleBtn.getStyleClass().add("success-button");
        completeSaleBtn.setPrefHeight(40);

        clearCartBtn.prefWidthProperty().bind(cartButtons.widthProperty().multiply(0.4));
        completeSaleBtn.prefWidthProperty().bind(cartButtons.widthProperty().multiply(0.6));

        cartButtons.getChildren().addAll(clearCartBtn, completeSaleBtn);
        cartTableBox.getChildren().add(cartTable);
        cartContainer.getChildren().addAll(cartTableBox, cartButtons);

        // ---------- Layout Placement ----------
        contentArea.add(productsContainer, 0, 0);
        contentArea.add(cartContainer, 1, 0);
        layout.getChildren().add(contentArea);

        // =====================================
        // ========== SAMPLE PRODUCTS ==========
        // =====================================

        ObservableList<Product> products = FXCollections.observableArrayList(
                new Product("Notebook", 50.00, 100),
                new Product("Blue Pen", 15.00, 200),
                new Product("Binder", 120.00, 30),
                new Product("Pencil", 10.00, 150),
                new Product("Eraser", 12.50, 80),
                new Product("Stapler", 85.00, 40),
                new Product("Highlighters", 45.00, 120),
                new Product("Scissors", 65.00, 60)
        );
        productsTable.setItems(products);

        // =====================================
        // ========== EVENT HANDLERS ===========
        // =====================================

        addToCartBtn.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cartTable.getItems().add(new CartItem(selected, 1));
            }
        });

        clearCartBtn.setOnAction(e -> cartTable.getItems().clear());

        completeSaleBtn.setOnAction(e -> {
            if (cartTable.getItems().isEmpty()) {
                PopUpDialog.showError("Cart is empty!");
                return;
            }
            PopUpDialog.showInfo("Sale completed successfully!");
            parentLayout.setCenter(EmployeeSalesLayout.build(parentLayout));
        });

        searchField.prefWidthProperty().bind(productsTable.widthProperty());

        return layout;
    }

    // ============================================
    // ========== PRODUCTS TABLE CREATION =========
    // ============================================

    private static TableView<Product> createProductsTable() {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().addAll("table-view", "expand");

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(nameCol, priceCol, stockCol);
        return table;
    }

    // ==========================================
    // ========== CART TABLE CREATION ===========
    // ==========================================

    private static TableView<CartItem> createCartTable() {
        TableView<CartItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().addAll("table-view", "expand");
        table.setMaxHeight(Double.MAX_VALUE);

        TableColumn<CartItem, String> productCol = new TableColumn<>("Item");
        productCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProduct().getName()));
        productCol.setPrefWidth(150);

        TableColumn<CartItem, Void> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellFactory(col -> new TableCell<>() {
            private final HBox container = new HBox(5);
            private final TextField quantityField = new TextField();
            private final Button minusBtn = new Button("-");
            private final Button plusBtn = new Button("+");

            {
                quantityField.setPrefWidth(50);
                quantityField.getStyleClass().add("quantity-input");
                quantityField.setTextFormatter(new TextFormatter<>(change ->
                        change.getText().matches("\\d*") ? change : null));

                minusBtn.getStyleClass().add("quantity-btn");
                plusBtn.getStyleClass().add("quantity-btn");

                minusBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null && item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        quantityField.setText(String.valueOf(item.getQuantity()));
                    }
                });

                plusBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        item.setQuantity(item.getQuantity() + 1);
                        quantityField.setText(String.valueOf(item.getQuantity()));
                    }
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(minusBtn, quantityField, plusBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CartItem cartItem = getTableRow().getItem();
                    quantityField.setText(String.valueOf(cartItem.getQuantity()));
                    setGraphic(container);
                }
            }
        });

        TableColumn<CartItem, Void> deleteCol = new TableColumn<>("");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");

            {
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setPrefWidth(30);
                deleteBtn.setFocusTraversable(false);
                deleteBtn.setStyle("-fx-background-insets: 0;");
                deleteBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        getTableView().getItems().remove(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        table.getColumns().addAll(productCol, quantityCol, deleteCol);
        return table;
    }
}
