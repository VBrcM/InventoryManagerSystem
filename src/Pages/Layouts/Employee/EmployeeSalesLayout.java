package Pages.Layouts.Employee;

import DB.*;
import Dialogs.*;
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
import java.time.LocalDateTime;
import java.util.logging.Logger;

import static DB.AppFormatter.formatCurrency;

public class EmployeeSalesLayout {

    private static final Logger LOGGER = Logger.getLogger(EmployeeSalesLayout.class.getName());

    /**
     * Builds and returns the full employee sales layout.
     * Includes product list, cart, and transaction logic.
     */
    public static VBox build(BorderPane parentLayout) {
        VBox layout = new VBox();
        layout.setPadding(new Insets(20));
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Grid layout
        GridPane contentArea = new GridPane();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setHgap(20);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.ALWAYS);
        contentArea.getRowConstraints().add(rowConstraints);

        ColumnConstraints productsCol = new ColumnConstraints();
        productsCol.setPercentWidth(55);
        productsCol.setHgrow(Priority.ALWAYS);

        ColumnConstraints cartCol = new ColumnConstraints();
        cartCol.setPercentWidth(45);
        cartCol.setHgrow(Priority.ALWAYS);

        contentArea.getColumnConstraints().addAll(productsCol, cartCol);

        VBox productsBox = new VBox(10);
        VBox cartBox = new VBox(10);

        Label title = new Label("New Sale");
        title.setId("title-label");
        title.getStyleClass().add("title-label");
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

        // Filtering box
        HBox searchAndFilterBox = new HBox(10, searchField, categoryFilter);
        searchAndFilterBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setMaxWidth(Double.MAX_VALUE);

        ObservableList<Product> allProducts = FXCollections.observableArrayList();
        try {
            allProducts.addAll(ProductDAO.getAll());
        } catch (SQLException e) {
            LOGGER.severe("Failed to load products: " + e.getMessage());
            PopUpDialog.showError("Failed to load products:\n" + e.getMessage());
        }

        FilteredList<Product> filteredProducts = new FilteredList<>(allProducts, p -> true);

        // Populate category filter
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(
                allProducts.stream()
                        .map(Product::getCategoryName)
                        .distinct()
                        .sorted()
                        .toList()
        );
        categoryFilter.getSelectionModel().selectFirst();

        // Category filtering
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredProducts.setPredicate(product -> {
                boolean matchesSearch = product.getProductName().toLowerCase().contains(searchField.getText().toLowerCase());
                boolean matchesCategory = newVal == null || newVal.equals("All Categories") || product.getCategoryName().equals(newVal);
                return matchesSearch && matchesCategory;
            });
            LOGGER.info("Category selected: " + newVal);
        });

        // Text filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredProducts.setPredicate(product -> {
                boolean matchesSearch = product.getProductName().toLowerCase().contains(newVal.toLowerCase());
                boolean matchesCategory = categoryFilter.getValue() == null
                        || categoryFilter.getValue().equals("All Categories")
                        || product.getCategoryName().equals(categoryFilter.getValue());
                return matchesSearch && matchesCategory;
            });
            LOGGER.info("Search updated: " + newVal);
        });

        TableView<Product> productsTable = createProductsTable(); // Product table
        productsTable.setItems(filteredProducts);

        ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
        TableView<CartItem> cartTable = createCartTable(cartItems); // Cart table
        cartTable.setItems(cartItems);

        Label totalLabel = new Label();
        totalLabel.getStyleClass().add("total-amount");
        bindTotalLabel(totalLabel, cartItems); // Bind total
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

        // Add selected product to cart
        addToCartBtn.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                for (CartItem item : cartItems) {
                    if (item.getProduct().getProductId() == selected.getProductId()) {
                        item.setQuantity(item.getQuantity() + 1);
                        cartTable.refresh();
                        LOGGER.info("Increased quantity for: " + selected.getProductName());
                        return;
                    }
                }
                cartItems.add(new CartItem(selected, 1));
                cartTable.refresh();
                LOGGER.info("Added product to cart: " + selected.getProductName());
            } else {
                LOGGER.info("No product selected");
            }
        });

        // Clear cart
        clearCartBtn.setOnAction(e -> {
            cartItems.clear();
            LOGGER.info("Cart cleared.");
        });

        // Complete sale transaction
        completeSaleBtn.setOnAction(e -> {
            if (cartItems.isEmpty()) {
                PopUpDialog.showError("Cart is empty.");
                return;
            }

            Connection conn = null;

            try {
                conn = JDBC.connect();
                conn.setAutoCommit(false); // Begin transaction

                int totalQty = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
                double totalAmount = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
                LocalDateTime now = LocalDateTime.now();

                Sale sale = new Sale();
                sale.setSaleQty(totalQty);
                sale.setTotalAmount(totalAmount);
                sale.setSaleDate(now);

                int saleId = SaleDAO.insertSale(conn, sale, cartItems);
                if (saleId <= 0) {
                    conn.rollback();
                    PopUpDialog.showError("Failed to complete sale.");
                    return;
                }

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

                conn.commit();
                PopUpDialog.showSuccess("Sale completed!");
                LOGGER.info("Sale completed with ID: " + saleId);

                cartItems.clear();

                // Refresh product list
                try {
                    allProducts.setAll(ProductDAO.getAll());
                    productsTable.refresh();
                } catch (SQLException ex) {
                    LOGGER.severe("Failed to reload products: " + ex.getMessage());
                    PopUpDialog.showError("Failed to reload products:\n" + ex.getMessage());
                }

            } catch (Exception ex) {
                LOGGER.severe("Transaction failed: " + ex.getMessage());
                try {
                    if (conn != null) conn.rollback();
                } catch (Exception rollbackEx) {
                    LOGGER.severe("Rollback failed: " + rollbackEx.getMessage());
                    PopUpDialog.showError("Rollback failed: " + rollbackEx.getMessage());
                }
                PopUpDialog.showError("Transaction failed: " + ex.getMessage());
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                } catch (Exception closeEx) {
                    LOGGER.warning("Failed to close connection: " + closeEx.getMessage());
                }
            }
        });

        // Layout assembly
        VBox.setVgrow(productsTable, Priority.ALWAYS);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        productsBox.getChildren().addAll(title, searchAndFilterBox, productsTable, addToCartBtn);
        VBox.setVgrow(productsBox, Priority.ALWAYS);
        VBox.setVgrow(cartBox, Priority.ALWAYS);
        cartBox.getChildren().addAll(cartTable, totalBox, cartButtons);

        contentArea.add(productsBox, 0, 0);
        contentArea.add(cartBox, 1, 0);

        layout.getChildren().add(contentArea);
        layout.getStyleClass().add("root-panel");
        layout.setAlignment(Pos.TOP_CENTER);

        return layout;
    }

    /**
     * Creates and returns the product table view.
     */
    private static TableView<Product> createProductsTable() {
        // Table for displaying products
        TableView<Product> productsTable = new TableView<>();
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));

        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getProductPrice();
            return new SimpleStringProperty(formatCurrency(price));
        });

        TableColumn<Product, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getStock();
            return new SimpleStringProperty(AppFormatter.formatNumber(stock));
        });

        productsTable.getColumns().addAll(nameCol, priceCol, stockCol);
        return productsTable;
    }

    /**
     * Creates and returns the cart table view.
     */
    private static TableView<CartItem> createCartTable(ObservableList<CartItem> cartItems) {
        // Table for displaying cart items
        TableView<CartItem> cartTable = new TableView<>();
        cartTable.setPrefHeight(300);
        cartTable.setFixedCellSize(60);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Manual sizing

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
                quantityField.setPrefWidth(70);
                quantityField.setMaxWidth(70);
                quantityField.setMinHeight(40);
                quantityField.setPrefHeight(40);
                quantityField.setMaxHeight(40);
                quantityField.setStyle("-fx-font-size: 12px;");
                HBox.setMargin(quantityField, new Insets(2, 0, 0, 0));
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
            return new SimpleStringProperty(formatCurrency(subtotal));
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

        cartTable.getColumns().addAll(productCol, quantityCol, subtotalCol, deleteCol);
        cartTable.setRowFactory(tv -> {
            TableRow<CartItem> row = new TableRow<>();
            row.setPrefHeight(50); // Adjust this value as needed
            return row;
        });
        return cartTable;
    }

    /**
     * Binds total label to dynamically show current cart total.
     */
    private static void bindTotalLabel(Label totalLabel, ObservableList<CartItem> cartItems) {
        Runnable rebinder = () -> {
            Observable[] dependencies = cartItems.stream()
                    .map(CartItem::quantityProperty)
                    .toArray(Observable[]::new);

            totalLabel.textProperty().bind(Bindings.createStringBinding(() -> {
                double total = cartItems.stream()
                        .mapToDouble(CartItem::getTotal)
                        .sum();
                return "Total: " + formatCurrency(total);
            }, dependencies));
        };

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