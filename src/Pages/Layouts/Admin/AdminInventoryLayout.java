package Pages.Layouts.Admin;

import DB.*;
import Dialogs.*;
import Model.DAO.ProductDAO;
import Model.POJO.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminInventoryLayout {

    /**
     * Builds the default Inventory page showing all products.
     */
    public static StackPane build() {
        return build(false);
    }

    /**
     * Builds the Inventory page with an option to show only out-of-stock items.
     *
     * @param showOnlyOutOfStock whether to show only products with 0 stock
     * @return StackPane containing the full layout
     */
    public static StackPane build(boolean showOnlyOutOfStock) {
        // ===== Title =====
        Label title = new Label("Inventory");
        title.setId("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(10));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // ===== Search Field =====
        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");

        // ===== Category Filter =====
        ProductDAO dao = new ProductDAO();
        List<Product> productList = new ArrayList<>();
        try {
            productList = dao.getAll();
        } catch (SQLException e) {
            e.printStackTrace(); // or show an error dialog
        }

        if (showOnlyOutOfStock) {
            productList.removeIf(p -> p.getStock() > 0);
        }

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

        // ===== Filters Layout =====
        HBox filtersBox = new HBox(10, searchField, categoryFilter);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setPadding(new Insets(10, 0, 10, 0));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setMaxWidth(Double.MAX_VALUE);

        // ===== Table Setup =====
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
                setText(empty || item == null ? null : Formatter.formatCurrency(item));
            }
        });

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ===== Data Binding and Filtering =====
        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        FilteredList<Product> filteredList = new FilteredList<>(products, p -> true);

        Runnable updateFilter = () -> {
            String searchText = searchField.getText().toLowerCase();
            String selectedCategory = categoryFilter.getValue();

            filteredList.setPredicate(p -> {
                boolean matchesSearch = p.getProductName().toLowerCase().contains(searchText) ||
                        p.getCategoryName().toLowerCase().contains(searchText);
                boolean matchesCategory = "All Categories".equals(selectedCategory) ||
                        selectedCategory.equals(p.getCategoryName());
                return matchesSearch && matchesCategory;
            });
        };

        // Update filter when search or category changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());

        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);

        // ===== Action Buttons =====
        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        for (Button btn : List.of(addBtn, editBtn, deleteBtn)) {
            btn.getStyleClass().add("inventory-button");
            btn.setPrefSize(200, 60);
        }

        HBox actionButtons = new HBox(20, addBtn, editBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));

        // ===== Content Layout =====
        VBox content = new VBox(10, title, filtersBox, table, actionButtons);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #1e1e1e;");

        StackPane root = new StackPane(content);

        // ===== Button Logic =====

        // Add new product
        addBtn.setOnAction(e -> InventoryDialog.show(null, products, () -> {
            table.refresh();
            updateFilter.run();
        }));

        // Edit selected product
        editBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                InventoryDialog.show(selected, products, () -> {
                    try {
                        List<Product> refreshed = dao.getAll(); // this was missing
                        if (showOnlyOutOfStock) {
                            refreshed.removeIf(p -> p.getStock() > 0);
                        }
                        products.setAll(refreshed);
                        updateFilter.run();
                        table.refresh();
                    } catch (SQLException ex) {
                        ex.printStackTrace(); // Or show error dialog
                        PopUpDialog.showError("Database Error");
                    }
                });
            } else {
                PopUpDialog.showError("Please select a product to edit.");
            }
        });

        // Delete selected product
        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                PopUpDialog.showConfirmation("Delete Item", "Are you sure you want to delete this item?", () -> {
                    try {
                        dao.delete(selected.getProductId());
                        products.remove(selected);
                        PopUpDialog.showInfo("Product deleted successfully.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        PopUpDialog.showError("An unexpected error occurred: " + ex.getMessage());
                    }
                });
            } else {
                PopUpDialog.showError("Please select a product to delete.");
            }
        });

        return root;
    }
}