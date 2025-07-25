package Pages.Layouts.Admin;

import DB.*;
import Model.DAO.*;
import Model.POJO.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds and returns a layout displaying the detailed sales report for a specific date.
 * Includes sale items, categories, quantities, prices, and calculated total.
 */
public class AdminReportDetailsLayout {

    private static final Logger LOGGER = Logger.getLogger(AdminReportDetailsLayout.class.getName());

    /**
     * Returns a VBox layout that presents detailed sales data for the given date.
     * Includes a table of sold items, total calculation, and navigation controls.
     */
    public static VBox build(BorderPane parentLayout, LocalDate date) {
        // Title
        Label title = new Label("Report Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER_LEFT);
        title.setPadding(new Insets(0, 0, 10, 0));

        // Table
        TableView<SaleItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<SaleItem, Integer> saleIdCol = new TableColumn<>("Sale ID");
        saleIdCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));

        TableColumn<SaleItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<SaleItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<SaleItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("siQty"));
        quantityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : AppFormatter.formatNumber(val));
            }
        });

        TableColumn<SaleItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("siPrice"));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : AppFormatter.formatCurrency(val));
            }
        });

        TableColumn<SaleItem, Double> totalCol = new TableColumn<>("Total Price");
        totalCol.setCellValueFactory(cellData -> {
            SaleItem item = cellData.getValue();
            double total = item.getSiQty() * item.getSiPrice();
            return new javafx.beans.property.SimpleDoubleProperty(total).asObject();
        });
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : AppFormatter.formatCurrency(val));
            }
        });

        table.getColumns().addAll(saleIdCol, nameCol, categoryCol, quantityCol, priceCol, totalCol);

        // Load data
        try {
            List<SaleItem> items = SaleItemDAO.getSaleItemsByDate(date);
            table.getItems().setAll(items);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load sale items for date: " + date, e);
        }

        VBox.setVgrow(table, Priority.ALWAYS);

        // Total calculation
        double grandTotal = table.getItems().stream()
                .mapToDouble(item -> item.getSiQty() * item.getSiPrice())
                .sum();

        Label totalLabel = new Label("Total Amount: " + AppFormatter.formatCurrency(grandTotal));
        totalLabel.getStyleClass().add("total-amount");

        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        // Back button
        Button backBtn = new Button("Back to Reports");
        backBtn.getStyleClass().add("inventory-button");
        backBtn.setPrefSize(200, 50);
        backBtn.setOnAction(e -> parentLayout.setCenter(AdminReportLayout.build(parentLayout)));

        HBox buttonBox = new HBox(backBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Final layout
        VBox layout = new VBox(20, title, table, totalBox, buttonBox);
        layout.getStyleClass().add("root-panel");
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);

        return layout;
    }
}