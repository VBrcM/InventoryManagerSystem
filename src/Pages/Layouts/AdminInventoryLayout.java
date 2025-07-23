package Pages.Layouts;

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

import java.util.List;

public class AdminInventoryLayout {

    public static StackPane build() {
        return build(false);  // Default view: show all products
    }

    public static StackPane build(boolean showOnlyOutOfStock) {
        Label title = new Label("Inventory");
        title.setId("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(10, 0, 10, 0));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");

        // ProductDAO to fetch data
        ProductDAO dao = new ProductDAO();
        List<Product> productList = dao.getAll();

        if (showOnlyOutOfStock) {
            productList.removeIf(p -> p.getStock() > 0);
        }

        // Extract unique category names for ComboBox filter
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
        categoryFilter.setPrefWidth(200);
        categoryFilter.setPrefHeight(38);

        // Put searchField and categoryFilter side-by-side
        HBox filtersBox = new HBox(10);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setPadding(new Insets(10, 0, 10, 0));

        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setMaxWidth(Double.MAX_VALUE);


        filtersBox.getChildren().addAll(searchField, categoryFilter);

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
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", item));
                }
            }
        });

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(Formatter.formatCurrency(item));
                }
            }
        });

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        VBox.setVgrow(table, Priority.ALWAYS);


        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        FilteredList<Product> filteredList = new FilteredList<>(products, p -> true);

        // Update filtering predicate based on search text and selected category
        Runnable updateFilter = () -> {
            String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            String selectedCategory = categoryFilter.getValue();

            filteredList.setPredicate(p -> {
                boolean matchesSearch = p.getProductName() != null && p.getProductName().toLowerCase().contains(searchText)
                        || p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(searchText);

                boolean matchesCategory = "All Categories".equals(selectedCategory)
                        || (p.getCategoryName() != null && p.getCategoryName().equals(selectedCategory));

                return matchesSearch && matchesCategory;
            });
        };

        // Add listeners for both searchField and categoryFilter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());

        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        addBtn.getStyleClass().add("inventory-button");
        editBtn.getStyleClass().add("inventory-button");
        deleteBtn.getStyleClass().add("inventory-button");

        addBtn.setPrefHeight(60);
        editBtn.setPrefHeight(60);
        deleteBtn.setPrefHeight(60);

        addBtn.setPrefWidth(200);
        editBtn.setPrefWidth(200);
        deleteBtn.setPrefWidth(200);

        HBox actionButtons = new HBox(20, addBtn, editBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));

        VBox content = new VBox(10, title, filtersBox, table, actionButtons);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setStyle("-fx-background-color: #1e1e1e;");

        StackPane root = new StackPane(content);

        addBtn.setOnAction(e -> InventoryDialog.show(null, products, () -> {
            table.refresh();
            updateFilter.run();  // re-apply filter on refresh
        }));

        editBtn.setOnAction(e -> {
            Product selectedProduct = table.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                InventoryDialog.show(selectedProduct, products, () -> {
                    List<Product> refreshedProducts = dao.getAll();

                    if (showOnlyOutOfStock) {
                        refreshedProducts.removeIf(p -> p.getStock() > 0);
                    }

                    products.clear();
                    products.addAll(refreshedProducts);

                    updateFilter.run();
                    table.refresh();
                });
            } else {
                PopUpDialog.showError("Please select a product to edit.");
            }
        });

        deleteBtn.setOnAction(e -> {
            Product selectedProduct = table.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                PopUpDialog.showConfirmation("Delete Item", "Are you sure you want to delete this item?", () -> {
                    try {
                        dao.delete(selectedProduct.getProductId());
                        products.remove(selectedProduct);
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