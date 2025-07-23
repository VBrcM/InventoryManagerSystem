package Pages.Layouts;

import DB.Formatter;
import Model.DAO.ProductDAO;
import Model.POJO.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class EmployeeInventoryLayout {

    public static StackPane build() {
        return build(false); // Default: show all
    }

    public static StackPane build(boolean showOnlyLowStock) {
        Label title = new Label("Inventory");
        title.setId("title-label");

        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");

        ProductDAO dao = new ProductDAO();
        List<Product> productList = dao.getAll();

        if (showOnlyLowStock) {
            productList.removeIf(p -> p.getStock() > 0);
        }

        // Category filter ComboBox
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

        HBox filtersBox = new HBox(10, searchField, categoryFilter);
        filtersBox.setAlignment(Pos.CENTER_LEFT);

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

        // Filtered + sorted data
        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        FilteredList<Product> filteredList = new FilteredList<>(products, p -> true);

        Runnable updateFilter = () -> {
            String search = searchField.getText().toLowerCase();
            String selectedCategory = categoryFilter.getValue();

            filteredList.setPredicate(p -> {
                boolean matchesSearch = (p.getProductName() != null && p.getProductName().toLowerCase().contains(search)) ||
                        (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(search));
                boolean matchesCategory = "All Categories".equals(selectedCategory) ||
                        (p.getCategoryName() != null && p.getCategoryName().equals(selectedCategory));
                return matchesSearch && matchesCategory;
            });
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());

        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);

        // Low stock styling
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                getStyleClass().remove("low-stock");
                setTooltip(null);
                if (product != null && !empty && product.getStock() < 10) {
                    getStyleClass().add("low-stock");
                    setTooltip(new Tooltip("Low stock! Consider restocking soon."));
                }
            }
        });

        VBox content = new VBox(20, title, filtersBox, table);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e1e1e;");

        // Scrollable wrapper
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-border-color: transparent;
            -fx-background-insets: 0;
            -fx-padding: 0;
        """);

        // Final wrapper pane with matching background
        StackPane root = new StackPane(scrollPane);
        root.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: transparent;");

        return root;
    }
}
