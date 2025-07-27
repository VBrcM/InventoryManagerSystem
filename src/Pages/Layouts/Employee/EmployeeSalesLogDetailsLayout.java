package Pages.Layouts.Employee;

import DB.*;
import Dialogs.*;
import Model.DAO.*;
import Model.POJO.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Layout for displaying detailed transaction logs for a specific date.
 * Shows sales in a table with item summary and total.
 */
public class EmployeeSalesLogDetailsLayout {

    private static final Logger logger = Logger.getLogger(EmployeeSalesLogDetailsLayout.class.getName());

    /**
     * Builds the transaction details layout for a specific date.
     * Contains a table view of sales, total calculation, and back button.
     */
    public static VBox build(BorderPane parentLayout, LocalDate date, List<Sale> sales){
        logger.info("Building Transaction Details Layout for date: " + date);

        Label title = new Label("Transaction Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");

        // Table setup
        TableView<Sale> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Row click opens receipt dialog
        table.setRowFactory(tv -> {
            TableRow<Sale> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Sale sale = row.getItem();
                    showSaleDetails(sale);
                }
            });
            return row;
        });

        // Sale ID column
        TableColumn<Sale, Number> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSaleId()));
        idCol.setPrefWidth(80);

        // Time column
        TableColumn<Sale, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(AppFormatter.formatTime(cellData.getValue().getSaleDate()))
        );
        timeCol.setPrefWidth(100);

        // Items column
        TableColumn<Sale, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> {
            List<String> itemStrings = new ArrayList<>();
            for (SaleItem item : cellData.getValue().getSaleItems()) {
                itemStrings.add(item.getProduct().getProductName() + " (" + item.getSiQty() + ")");
            }
            return new SimpleStringProperty(String.join(", ", itemStrings));
        });
        itemsCol.setPrefWidth(640);

        // Total column
        TableColumn<Sale, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(AppFormatter.formatCurrency(cellData.getValue().getTotalAmount()))
        );
        totalCol.setPrefWidth(120);


        // Add columns and data to table
        table.getColumns().addAll(idCol, timeCol, itemsCol, totalCol);
        table.getItems().addAll(sales);

        // Calculate grand total
        double grandTotal = sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();

        Label totalLabel = new Label("Total Amount: " + AppFormatter.formatCurrency(grandTotal));
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        // Back button returns to sales log
        Button backBtn = new Button("Back to Log");
        backBtn.setOnAction(e -> {
            logger.info("Back button clicked. Returning to transaction log.");
            parentLayout.setCenter(EmployeeSalesLogLayout.build(parentLayout));
        });
        backBtn.getStyleClass().add("inventory-button");

        // Final layout setup
        VBox root = new VBox(20, title, table, totalBox, backBtn);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root-panel");
        root.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    /**
     * Shows receipt content for the selected sale.
     */
    private static void showSaleDetails(Sale sale) {
        List<SaleItem> items = SaleItemDAO.getSaleItemsBySaleId(sale.getSaleId());
        int itemCount = items.stream().mapToInt(SaleItem::getSiQty).sum();
        ReceiptDialog.showContent(sale.getSaleDate(), items, itemCount);
    }
}