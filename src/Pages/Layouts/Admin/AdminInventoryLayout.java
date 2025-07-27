package Pages.Layouts.Admin;

import DB.*;
import Dialogs.*;
import Model.DAO.*;
import Model.POJO.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Layout for displaying and managing the inventory table in the admin view.
 * This layout provides category filtering, search capability, and action buttons
 * for creating, editing, and deleting inventory items.
 */
public class AdminInventoryLayout {

    private static final Logger logger = Logger.getLogger(AdminInventoryLayout.class.getName());

    // Builds the full inventory layout
    public static StackPane build() {
        return build(false);
    }

    // Builds the layout with optional filter for out-of-stock items
    public static StackPane build(boolean showLowOutStock) {
        Label title = new Label("Inventory");
        title.setId("title-label");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");

        // DAO and data load
        List<Product> productList = new ArrayList<>();

        try {
            productList = ProductDAO.getAll();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load products from database", e);
            PopUpDialog.showError("Failed to load products.");
        }

        // Average stock per category
        Map<String, Double> avgStockPerCategory = productList.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategoryName,
                        Collectors.averagingInt(Product::getStock)
                ));

        if (showLowOutStock) {
            productList = productList.stream()
                    .filter(p -> {
                        double avg = avgStockPerCategory.getOrDefault(p.getCategoryName(), 0.0);
                        return p.getStock() == 0 || p.getStock() < avg * 0.2;
                    })
                    .collect(Collectors.toList());
        }

        // Category filter
        List<String> categories = productList.stream()
                .map(Product::getCategoryName)
                .distinct()
                .sorted()
                .toList();

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(categories);
        categoryFilter.setValue("All Categories");
        categoryFilter.getStyleClass().add("inventory-button");
        categoryFilter.setPrefSize(200, 38);

        HBox filtersBox = new HBox(10, searchField, categoryFilter);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Table
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Product, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        quantityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,d", item));
            }
        });

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : AppFormatter.formatCurrency(item));
            }
        });

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Filtering
        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        FilteredList<Product> filteredList = new FilteredList<>(products, p -> true);

        Runnable updateFilter = () -> {
            String searchText = searchField.getText().toLowerCase();
            String selectedCategory = categoryFilter.getValue();
            filteredList.setPredicate(p -> {
                boolean matchesSearch = p.getProductName().toLowerCase().contains(searchText)
                        || p.getCategoryName().toLowerCase().contains(searchText);
                boolean matchesCategory = "All Categories".equals(selectedCategory)
                        || selectedCategory.equals(p.getCategoryName());
                return matchesSearch && matchesCategory;
            });
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());

        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);

        // Row styling based on stock level
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                getStyleClass().removeAll("low-stock", "out-of-stock");

                if (product != null && !empty) {
                    int stock = product.getStock();
                    double avg = avgStockPerCategory.getOrDefault(product.getCategoryName(), 0.0);
                    double threshold = avg * 0.2;

                    if (stock == 0) {
                        getStyleClass().add("out-of-stock");
                        setTooltip(new Tooltip("Out of stock"));
                    } else if (stock < threshold) {
                        getStyleClass().add("low-stock");
                        setTooltip(new Tooltip("Low stock: below 20% of category average"));
                    } else {
                        setTooltip(null);
                    }
                } else {
                    setTooltip(null);
                }
            }
        });

        // Action buttons
        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        for (Button btn : List.of(addBtn, editBtn, deleteBtn)) {
            btn.getStyleClass().add("inventory-button");
            btn.setPrefSize(200, 60);
        }

        HBox actionButtons = new HBox(20, addBtn, editBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER);

        VBox content = new VBox(20, title, filtersBox, table, actionButtons);
        content.getStyleClass().add("root-panel");
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);

        // Button Handlers
        addBtn.setOnAction(e -> InventoryDialog.show(null, products, () -> {
            try {
                List<Product> refreshed = ProductDAO.getAll();

                if (showLowOutStock) {
                    refreshed = refreshed.stream()
                            .filter(p -> {
                                double avg = avgStockPerCategory.getOrDefault(p.getCategoryName(), 0.0);
                                return p.getStock() == 0 || p.getStock() < avg * 0.2;
                            })
                            .collect(Collectors.toList());
                }

                products.setAll(refreshed);

                avgStockPerCategory.clear();
                avgStockPerCategory.putAll(refreshed.stream()
                        .collect(Collectors.groupingBy(
                                Product::getCategoryName,
                                Collectors.averagingInt(Product::getStock)
                        ))
                );

                refreshCategoryFilter(categoryFilter, refreshed);
                updateFilter.run();
                table.refresh();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Failed to refresh product list after add", ex);
                PopUpDialog.showError("Database Error");
            }
        }));


        editBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                InventoryDialog.show(selected, products, () -> {
                    try {
                        List<Product> refreshed = ProductDAO.getAll();
                        if (showLowOutStock) {
                            refreshed = refreshed.stream()
                                    .filter(p -> {
                                        double avg = avgStockPerCategory.getOrDefault(p.getCategoryName(), 0.0);
                                        return p.getStock() == 0 || p.getStock() < avg * 0.2;
                                    })
                                    .collect(Collectors.toList());
                        }
                        products.setAll(refreshed);
                        avgStockPerCategory.clear();
                        avgStockPerCategory.putAll(refreshed.stream()
                                .collect(Collectors.groupingBy(
                                        Product::getCategoryName,
                                        Collectors.averagingInt(Product::getStock)
                                ))
                        );
                        refreshCategoryFilter(categoryFilter, refreshed);
                        updateFilter.run();
                        table.refresh();
                    } catch (SQLException ex) {
                        logger.log(Level.SEVERE, "Failed to refresh inventory list after editing", ex);
                        PopUpDialog.showError("Database Error");
                    }
                });
            } else {
                PopUpDialog.showError("Please select a product to edit.");
            }
        });

        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                PopUpDialog.showConfirmation("Delete Item", "Are you sure you want to delete this item?", confirmed -> {
                    if (confirmed) {
                        try {
                            ProductDAO.delete(selected.getProductId());

                            // FULL REFRESH instead of just products.remove(selected)
                            List<Product> refreshed = ProductDAO.getAll();

                            if (showLowOutStock) {
                                refreshed = refreshed.stream()
                                        .filter(p -> {
                                            double avg = avgStockPerCategory.getOrDefault(p.getCategoryName(), 0.0);
                                            return p.getStock() == 0 || p.getStock() < avg * 0.2;
                                        })
                                        .collect(Collectors.toList());
                            }

                            products.setAll(refreshed);

                            avgStockPerCategory.clear();
                            avgStockPerCategory.putAll(refreshed.stream()
                                    .collect(Collectors.groupingBy(
                                            Product::getCategoryName,
                                            Collectors.averagingInt(Product::getStock)
                                    ))
                            );

                            refreshCategoryFilter(categoryFilter, refreshed);
                            updateFilter.run();
                            table.refresh();

                            PopUpDialog.showSuccess("Product deleted successfully.");
                            logger.info("Deleted product ID: " + selected.getProductId());

                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Failed to delete product", ex);
                            PopUpDialog.showError("An unexpected error occurred: " + ex.getMessage());
                        }
                    }
                });
            } else {
                PopUpDialog.showError("Please select a product to delete.");
            }
        });

        // Root wrapper
        return new StackPane(content);
    }
    private static void refreshCategoryFilter(ComboBox<String> categoryFilter, List<Product> productList) {
        List<String> updatedCategories = productList.stream()
                .map(Product::getCategoryName)
                .distinct()
                .sorted()
                .toList();

        categoryFilter.getItems().setAll("All Categories");
        categoryFilter.getItems().addAll(updatedCategories);

        // If previous value no longer exists, reset to "All Categories"
        if (!categoryFilter.getItems().contains(categoryFilter.getValue())) {
            categoryFilter.setValue("All Categories");
        }
    }

}