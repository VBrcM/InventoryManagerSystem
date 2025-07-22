package Pages.Layouts;

import DB.*;
import Dialogs.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class EmployeeTransactionLayout {

    public static StackPane build() {
        return build(false);  // Default: show all products
    }

    public static StackPane build(boolean showOnlyOutOfStock) {
        Label title = new Label("Product Stock Transaction");
        title.setId("title-label");

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.getStyleClass().add("input-field");
        searchField.setMaxWidth(200);

        // Category filter
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(new CategoryDAO().getAllCategoryNames());
        categoryFilter.setValue("All Categories");
        categoryFilter.getStyleClass().add("inventory-button");

        // Spacer for alignment
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Top bar: search left, category filter right
        HBox topBar = new HBox(10, searchField, spacer, categoryFilter);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Table setup
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("product"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Quantity");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setCellFactory(col -> new TableCell<>() {
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
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
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

        table.getColumns().addAll(nameCol, categoryCol, priceCol, stockCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Product data
        ProductDAO dao = new ProductDAO();
        List<Product> productList = dao.getAllWithCategory();

        if (showOnlyOutOfStock) {
            productList.removeIf(p -> p.getStock() > 0);
        }

        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        FilteredList<Product> filteredList = new FilteredList<>(products, p -> true);
        table.setItems(filteredList);

        // Filter logic (search + category)
        Runnable applyFilters = () -> {
            String keyword = searchField.getText().toLowerCase();
            String selectedCategory = categoryFilter.getValue();

            filteredList.setPredicate(p -> {
                String name = p.getProduct() != null ? p.getProduct().toLowerCase() : "";
                String category = p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "";
                boolean matchesSearch = name.contains(keyword) || category.contains(keyword);
                boolean matchesCategory = selectedCategory.equals("All Categories") ||
                        category.equalsIgnoreCase(selectedCategory);
                return matchesSearch && matchesCategory;
            });
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        categoryFilter.setOnAction(e -> applyFilters.run());

        // Action buttons
        Button addBtn = new Button("Add Stock");
        Button reduceBtn = new Button("Reduce Stock");

        addBtn.getStyleClass().add("inventory-button");
        reduceBtn.getStyleClass().add("inventory-button");

        HBox actionButtons = new HBox(20, addBtn, reduceBtn);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));

        VBox content = new VBox(20, title, topBar, table, actionButtons);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e1e1e;");

        StackPane root = new StackPane(content);

        // Button handlers
        addBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TransactionDialog.show("add", selected, (qty)-> {
                    try {
                        List<Product> refreshed = dao.getAllWithCategory();
                        if (showOnlyOutOfStock) {
                            refreshed.removeIf(p -> p.getStock() > 0);
                        }
                        products.setAll(refreshed);
                        filteredList.setPredicate(filteredList.getPredicate());
                        table.refresh();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        PopUpDialog.showError("Failed to refresh inventory.");
                    }
                });
            } else {
                PopUpDialog.showError("Please select a product to add stock.");
            }
        });

        reduceBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TransactionDialog.show("reduce", selected, (qty) -> {
                    try {
                        List<Product> refreshed = dao.getAllWithCategory();
                        if (showOnlyOutOfStock) {
                            refreshed.removeIf(p -> p.getStock() > 0);
                        }
                        products.setAll(refreshed);
                        filteredList.setPredicate(filteredList.getPredicate());
                        table.refresh();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        PopUpDialog.showError("Failed to refresh inventory.");
                    }
                });
            } else {
                PopUpDialog.showError("Please select a product to reduce stock.");
            }
        });

        return root;
    }
}
