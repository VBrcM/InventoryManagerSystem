package Pages.Layouts.Employee;

import DB.Formatter;
import DB.JDBC;
import Dialogs.PopUpDialog;
import Model.DAO.*;
import Model.POJO.*;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class EmployeeSalesLayout {

    private ObservableList<CartItem> finalcartItems = FXCollections.observableArrayList();

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
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(10, 0, 20, 0));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

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

        ObservableList<Product> allProducts = FXCollections.observableArrayList();

        try {
            allProducts.addAll(ProductDAO.getAll());
        } catch (SQLException e) {
            e.printStackTrace();
            PopUpDialog.showError("Failed to load products:\n" + e.getMessage());
        }

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
                PopUpDialog.showError("Cart is empty.");
                return;
            }

            Connection conn = null;

            try {
                conn = JDBC.connect();
                conn.setAutoCommit(false); // Start transaction

                // 1. Prepare data
                int totalQty = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
                double totalAmount = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
                LocalDateTime now = LocalDateTime.now();

                // 2. Create Sale object
                Sale sale = new Sale();
                sale.setSaleQty(totalQty);
                sale.setTotalAmount(totalAmount);
                sale.setSaleDate(now);

                // 3. Insert sale + items
                int saleId = SaleDAO.insertSale(conn, sale, cartItems);
                if (saleId <= 0) {
                    conn.rollback();
                    PopUpDialog.showError("Failed to complete sale.");
                    return;
                }

                // 4. Update stock
                for (CartItem item : cartItems) {
                    Product product = item.getProduct();
                    int qty = item.getQuantity();
                    int productId = product.getProductId();

                    if (qty > product.getStock()) {
                        conn.rollback();
                        PopUpDialog.showError("Not enough stock for: " + product.getProductName());
                        return;
                    }

                    if (!ProductDAO.reduceStock(conn, productId, qty)) {
                        conn.rollback();
                        PopUpDialog.showError("Failed to reduce stock for: " + product.getProductName());
                        return;
                    }
                }

                // 5. Commit all changes
                conn.commit();
                PopUpDialog.showInfo("Sale completed!");

                cartItems.clear();

                // 6. Refresh product table
                try {
                    allProducts.setAll(ProductDAO.getAll());
                    productsTable.refresh();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    PopUpDialog.showError("Failed to reload products:\n" + ex.getMessage());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    if (conn != null) conn.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                    PopUpDialog.showError("Rollback failed: " + rollbackEx.getMessage());
                }
                PopUpDialog.showError("Transaction failed: " + ex.getMessage());

            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true); // Always reset
                        conn.close();
                    }
                } catch (Exception closeEx) {
                    closeEx.printStackTrace();
                }
            }
        });

        // Final assembly
        VBox.setVgrow(productsTable, Priority.ALWAYS); // Stretch product table
        VBox.setVgrow(cartTable, Priority.ALWAYS);     // Stretch cart table

        productsBox.getChildren().addAll(title, searchAndFilterBox, productsTable, addToCartBtn);
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

        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getProductPrice();
            return new SimpleStringProperty(Formatter.formatCurrency(price));
        });

        TableColumn<Product, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getStock();
            return new SimpleStringProperty(Formatter.formatNumber(stock));
        });

        table.getColumns().addAll(nameCol, priceCol, stockCol);

        return table;
    }

    private static TableView<CartItem> createCartTable(ObservableList<CartItem> cartItems) {
        TableView<CartItem> table = new TableView<>();
        table.setPrefHeight(300);
        table.setFixedCellSize(60);
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
                // Field Styling
                quantityField.setPrefWidth(60);
                quantityField.setMaxWidth(60);
                quantityField.setMinHeight(40);
                quantityField.setPrefHeight(40);
                quantityField.setMaxHeight(40);
                HBox.setMargin(quantityField, new Insets(5, 0, 0, 0));
                quantityField.setAlignment(Pos.CENTER);
                quantityField.setTextFormatter(new TextFormatter<>(change ->
                        change.getText().matches("\\d*") ? change : null
                ));

                // Button Styling
                minusBtn.getStyleClass().add("quantity-btn");
                plusBtn.getStyleClass().add("quantity-btn");
                minusBtn.setPrefSize(30, 30);
                plusBtn.setPrefSize(30, 30);

                // Minus button action
                minusBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null && item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        quantityField.setText(String.valueOf(item.getQuantity()));
                        getTableView().refresh();
                    }
                });

                // Plus button action
                plusBtn.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null && item.getQuantity() < item.getProduct().getStock()) {
                        item.setQuantity(item.getQuantity() + 1);
                        quantityField.setText(String.valueOf(item.getQuantity()));
                        getTableView().refresh();
                    }
                });

                // Manual entry action
                quantityField.setOnAction(e -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        try {
                            int val = Integer.parseInt(quantityField.getText());
                            if (val > 0 && val <= item.getProduct().getStock()) {
                                item.setQuantity(val);
                                getTableView().refresh();
                            } else {
                                quantityField.setText(String.valueOf(item.getQuantity()));
                            }
                        } catch (NumberFormatException ex) {
                            quantityField.setText(String.valueOf(item.getQuantity()));
                        }
                    }
                });

                // HBox Styling
                container.setSpacing(5);
                container.setAlignment(Pos.CENTER); // center contents
                container.setFillHeight(true);
                container.setPadding(Insets.EMPTY); // remove any extra padding
                container.getChildren().addAll(minusBtn, quantityField, plusBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    quantityField.setText(String.valueOf(getTableRow().getItem().getQuantity()));
                    setGraphic(container);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // Avoid text alignment issues
                    setAlignment(Pos.CENTER); // Align the cell itself
                    setPadding(Insets.EMPTY); // Remove gap below
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
                deleteBtn.setPrefSize(24,24); // Smaller square button
                deleteBtn.setStyle("-fx-font-size: 15px; -fx-padding: 2px;");

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
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        table.getColumns().addAll(productCol, quantityCol, subtotalCol, deleteCol);
        table.setRowFactory(tv -> {
            TableRow<CartItem> row = new TableRow<>();
            row.setPrefHeight(50); // Adjust this value as needed
            return row;
        });
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