package Pages.Layouts;

import Dialogs.PopUpDialog;
import Model.DAO.*;
import Model.POJO.*;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class EmployeeSalesLayout {

    public static class CartItem {
        private final Product product;
        private final IntegerProperty quantity = new SimpleIntegerProperty();

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity.set(quantity); // use property
        }

        public Product getProduct() { return product; }
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
        }    }

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
        VBox productsBox = new VBox(10);
        VBox cartBox = new VBox(10);

        Label title = new Label("New Sale");
        title.setId("title-label");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.getStyleClass().add("input-field");

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Filter by category");
        categoryFilter.setMinWidth(200);
        categoryFilter.getStyleClass().add("inventory-button");

        HBox searchAndFilterBox = new HBox(10, searchField, categoryFilter);
        searchAndFilterBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setMaxWidth(Double.MAX_VALUE);

        ObservableList<Product> allProducts = FXCollections.observableArrayList(ProductDAO.getAll());
        FilteredList<Product> filteredProducts = new FilteredList<>(allProducts, p -> true);

        // 🐞 Debug: Print loaded product count
        System.out.println("[DEBUG] Total products loaded: " + allProducts.size());

        // Populate categories
        categoryFilter.getItems().add("All Categories"); // Add first
        categoryFilter.getItems().addAll(
                allProducts.stream()
                        .map(Product::getCategoryName)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
        );
        categoryFilter.getSelectionModel().selectFirst(); // Default to "All Categories"

        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredProducts.setPredicate(product -> {
                boolean matchesSearch = product.getProductName().toLowerCase().contains(searchField.getText().toLowerCase());
                boolean matchesCategory = newVal == null || newVal.equals("All Categories") || product.getCategoryName().equals(newVal);
                return matchesSearch && matchesCategory;
            });
            System.out.println("[DEBUG] Category selected: " + newVal);
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredProducts.setPredicate(product -> {
                boolean matchesSearch = product.getProductName().toLowerCase().contains(searchField.getText().toLowerCase());
                boolean matchesCategory = categoryFilter.getValue() == null
                        || categoryFilter.getValue().equals("All Categories")
                        || product.getCategoryName().equals(categoryFilter.getValue());
                return matchesSearch && matchesCategory;
            });
            System.out.println("[DEBUG] Search updated: " + newVal);
        });

        TableView<Product> productsTable = createProductsTable();
        productsTable.setItems(filteredProducts);

        ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
        TableView<CartItem> cartTable = createCartTable(cartItems);
        cartTable.setItems(cartItems); // Ensure table is bound to cartItems

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        bindTotalLabel(totalLabel, cartItems);
        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(5, 10, 0, 10));

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("primary-button");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setPrefHeight(40);

        Button clearCartBtn = new Button("🗑 Empty Cart");
        clearCartBtn.getStyleClass().add("danger-button");
        clearCartBtn.setPrefHeight(40);

        Button completeSaleBtn = new Button("Complete Sale");
        completeSaleBtn.getStyleClass().add("success-button");
        completeSaleBtn.setPrefHeight(40);

        HBox cartButtons = new HBox(10, clearCartBtn, completeSaleBtn);
        cartButtons.setAlignment(Pos.CENTER);
        clearCartBtn.prefWidthProperty().bind(cartButtons.widthProperty().multiply(0.4));
        completeSaleBtn.prefWidthProperty().bind(cartButtons.widthProperty().multiply(0.6));

        // 🛒 Add to Cart event
        addToCartBtn.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                for (CartItem item : cartItems) {
                    if (item.getProduct().getProductId() == selected.getProductId()) {
                        item.setQuantity(item.getQuantity() + 1);
                        cartTable.refresh();
                        System.out.println("[DEBUG] Increased quantity for: " + selected.getProductName());
                        return;
                    }
                }
                cartItems.add(new CartItem(selected, 1));
                cartTable.refresh();
                System.out.println("[DEBUG] Added new product to cart: " + selected.getProductName());
            } else {
                System.out.println("[DEBUG] No product selected");
            }
        });

        clearCartBtn.setOnAction(e -> {
            cartItems.clear();
            System.out.println("[DEBUG] Cart cleared.");
        });

        completeSaleBtn.setOnAction(e -> {
            if (cartItems.isEmpty()) {
                PopUpDialog.showError("Cart is empty!");
                return;
            }
            try {
                int saleQty = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
                double totalAmount = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
                int saleId = SaleDAO.insert(saleQty, totalAmount);

                for (CartItem item : cartItems) {
                    SaleItemDAO.insert(new SaleItem(
                            saleId,
                            item.getProduct().getProductId(),
                            item.getQuantity(),
                            item.getProduct().getProductPrice(),
                            LocalDate.now()
                    ));
                    ProductDAO.reduceStock(item.getProduct().getProductId(), item.getQuantity());
                }

                PopUpDialog.showInfo("Sale completed successfully!");
                System.out.println("[DEBUG] Sale completed. ID: " + saleId + ", Total: ₱" + totalAmount);
                parentLayout.setCenter(EmployeeSalesLayout.build(parentLayout)); // refresh
            } catch (Exception ex) {
                PopUpDialog.showError("Sale failed!");
                ex.printStackTrace();
            }
        });

        // Final assembly
        VBox.setVgrow(productsTable, Priority.ALWAYS); // Stretch product table
        VBox.setVgrow(cartTable, Priority.ALWAYS);     // Stretch cart table

        productsBox.getChildren().addAll(titleBox, searchAndFilterBox, productsTable, addToCartBtn);
        VBox.setVgrow(productsBox, Priority.ALWAYS);
        VBox.setVgrow(cartBox, Priority.ALWAYS);
        VBox.setVgrow(productsTable, Priority.ALWAYS);
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        cartBox.getChildren().addAll(cartTable, totalBox, cartButtons);

        contentArea.add(productsBox, 0, 0);
        contentArea.add(cartBox, 1, 0);

        layout.getChildren().add(contentArea);
        return layout;
    }

    private static TableView<Product> createProductsTable() {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // allows column width control

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("productPrice"));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        table.getColumns().addAll(nameCol, priceCol, stockCol);

        return table;
    }

    private static TableView<CartItem> createCartTable(ObservableList<CartItem> cartItems) {
        TableView<CartItem> table = new TableView<>();
        table.setPrefHeight(300);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Manual sizing

        // === Product Column ===
        TableColumn<CartItem, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getProductName()));
        productCol.setPrefWidth(180);

        // === Quantity Column (HBox with - field +) ===
        TableColumn<CartItem, Void> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setMinWidth(140);// enough for all 3 controls

        quantityCol.setCellFactory(col -> new TableCell<CartItem, Void>() {
            private final HBox container = new HBox(5);
            private final TextField quantityField = new TextField();
            private final Button minusBtn = new Button("-");
            private final Button plusBtn = new Button("+");

            {
                quantityField.setPrefWidth(50);
                quantityField.setMaxWidth(50);
                quantityField.setAlignment(Pos.CENTER);
                quantityField.setTextFormatter(new TextFormatter<>(change -> change.getText().matches("\\d*") ? change : null));

                minusBtn.getStyleClass().add("quantity-btn");
                plusBtn.getStyleClass().add("quantity-btn");

                minusBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null && item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        quantityField.setText(String.valueOf(item.getQuantity()));
                        table.refresh();
                    }
                });

                plusBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null && item.getQuantity() < item.getProduct().getStock()) {
                        item.setQuantity(item.getQuantity() + 1);
                        quantityField.setText(String.valueOf(item.getQuantity()));
                        table.refresh();
                    }
                });

                quantityField.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        try {
                            int val = Integer.parseInt(quantityField.getText());
                            if (val > 0 && val <= item.getProduct().getStock()) {
                                item.setQuantity(val);
                                table.refresh();
                            } else {
                                quantityField.setText(String.valueOf(item.getQuantity()));
                            }
                        } catch (NumberFormatException ex) {
                            quantityField.setText(String.valueOf(item.getQuantity()));
                        }
                    }
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(minusBtn, quantityField, plusBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    CartItem cartItem = getTableRow().getItem();
                    quantityField.setText(String.valueOf(cartItem.getQuantity()));
                    setGraphic(container);
                }
            }
        });

        // === Subtotal Column ===
        TableColumn<CartItem, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(data -> {
            double subtotal = data.getValue().getTotal();
            return new SimpleStringProperty(String.format("₱ %.2f", subtotal));
        });
        subtotalCol.setPrefWidth(100);

        // === Delete Button Column ===
        TableColumn<CartItem, Void> deleteCol = new TableColumn<>("");
        deleteCol.setPrefWidth(60);

        deleteCol.setCellFactory(col -> new TableCell<CartItem, Void>() {
            private final Button deleteBtn = new Button("🗑");

            {
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setPrefWidth(30);
                deleteBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        table.getItems().remove(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        table.getColumns().addAll(productCol, quantityCol, subtotalCol, deleteCol);
        return table;
    }
    private static void bindTotalLabel(Label totalLabel, ObservableList<CartItem> cartItems) {
        Runnable rebinder = () -> {
            Observable[] dependencies = cartItems.stream()
                    .map(CartItem::quantityProperty)
                    .toArray(Observable[]::new);

            totalLabel.textProperty().bind(Bindings.createStringBinding(() ->
                            "Total: ₱" + String.format("%.2f",
                                    cartItems.stream().mapToDouble(CartItem::getTotal).sum()),
                    dependencies));
        };

        // Rebind anytime the cart items list changes
        cartItems.addListener((javafx.collections.ListChangeListener<CartItem>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    rebinder.run();
                }
            }
        });

        rebinder.run(); // Initial bind
    }

}
