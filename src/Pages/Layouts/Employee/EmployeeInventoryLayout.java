package Pages.Layouts.Employee;

import DB.AppFormatter;
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

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Builds the employee-side inventory layout, including category filters,
 * search input, table display, and stock highlighting.
 * The table supports searching and category-based filtering.
 */
public class EmployeeInventoryLayout {

    private static final Logger logger = Logger.getLogger(EmployeeInventoryLayout.class.getName());

    /**
     * Builds the default inventory layout showing all items.
     */
    public static StackPane build() {
        return build(false);
    }

    /**
     * Builds the inventory layout with an option to show only low stock items.
     */
    public static StackPane build(boolean showOnlyLowStock) {
        // Title
        Label title = new Label("Inventory");
        title.setId("title-label");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");
        searchField.setMaxWidth(Double.MAX_VALUE);

        // Fetch product list
        List<Product> productList = new ArrayList<>();
        try {
            productList = ProductDAO.getAll();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch products from database", e);
        }

        if (showOnlyLowStock) {
            productList.removeIf(p -> p.getStock() > 0);
        }

        // Compute average stock per category
        Map<String, Double> avgStockPerCategory = productList.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategoryName,
                        Collectors.averagingInt(Product::getStock)
                ));

        // Category dropdown
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

        // Filters container
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

        // Sorting
        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedList);

        // Row styling
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                setStyle("");
                setTooltip(null);

                if (product != null && !empty) {
                    int stock = product.getStock();
                    double avg = avgStockPerCategory.getOrDefault(product.getCategoryName(), 0.0);
                    double threshold = avg * 0.2;

                    if (stock == 0) {
                        setStyle("-fx-background-color: #ff4d4d;");
                        setTooltip(new Tooltip("Out of stock"));
                    } else if (stock < threshold) {
                        setStyle("-fx-background-color: #ff9900;");
                        setTooltip(new Tooltip("Low stock: below 20% of category average"));
                    }
                }
            }
        });

        // Main layout
        VBox content = new VBox(20, title, filtersBox, table);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e1e1e;");

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

        StackPane root = new StackPane(scrollPane);
        root.getStyleClass().add("root-panel");
        root.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: transparent;");
        return root;
    }
}