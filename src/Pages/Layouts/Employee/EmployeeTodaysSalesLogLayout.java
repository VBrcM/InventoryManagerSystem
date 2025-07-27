package Pages.Layouts.Employee;

import DB.*;
import Dialogs.*;
import Model.DAO.*;
import Model.POJO.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Displays the employee view of today's transaction list.
 * Includes a table showing sale time, items sold, and total,
 * with row click support to show detailed receipt.
 */
public class EmployeeTodaysSalesLogLayout {
    private static final Logger logger = Logger.getLogger(EmployeeTodaysSalesLogLayout.class.getName());

    // Builds the layout for displaying today's transactions
    public static VBox build() {
        Label title = new Label("Today's Sales");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.getStyleClass().add("date-label");

        TableView<Sale> table = createTransactionTable(); // Table
        List<Sale> sales = loadTodaysSales(table); // Load data

        double grandTotal = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        Label totalLabel = new Label("Total Amount: " + AppFormatter.formatCurrency(grandTotal));
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, title, dateLabel, table, totalBox);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-panel");
        root.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    // Creates and configures the transaction table
    private static TableView<Sale> createTransactionTable() {
        TableView<Sale> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        // Sale ID column
        TableColumn<Sale, Number> idCol = new TableColumn<>("Sale ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSaleId()));
        idCol.setPrefWidth(80);

        // Time column
        TableColumn<Sale, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> {
            LocalDateTime dt = data.getValue().getSaleDate();
            String formatted = dt != null ? dt.format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A";
            return new SimpleStringProperty(formatted);
        });
        timeCol.setPrefWidth(100);

        // Items column
        TableColumn<Sale, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(data -> {
            String summary = data.getValue().getSaleItems().stream()
                    .map(i -> i.getProductName() + " (" + i.getSiQty() +")")
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(summary);
        });
        itemsCol.setPrefWidth(640);

        // Total column
        TableColumn<Sale, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data ->
                new SimpleStringProperty(AppFormatter.formatCurrency(data.getValue().getTotalAmount()))
        );
        totalCol.setPrefWidth(120);

        table.getColumns().addAll(idCol, timeCol, itemsCol, totalCol);

        // Row click shows receipt dialog
        table.setRowFactory(tv -> {
            TableRow<Sale> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Sale sale = row.getItem();
                    List<SaleItem> items = SaleItemDAO.getSaleItemsBySaleId(sale.getSaleId());
                    if (items != null && !items.isEmpty()) {
                        ReceiptDialog.showContent(sale.getSaleDate(), items, items.size());
                    }
                }
            });
            return row;
        });

        return table;
    }

    // Loads today's sales and populates the table
    private static List<Sale> loadTodaysSales(TableView<Sale> table) {
        ObservableList<Sale> salesObservable = FXCollections.observableArrayList();
        List<Sale> sales = SaleDAO.getSalesByDate(LocalDate.now());

        for (Sale sale : sales) {
            try {
                List<SaleItem> items = SaleItemDAO.getBySaleId(sale.getSaleId());
                sale.setSaleItems(items);
                salesObservable.add(sale);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error loading sale items for sale ID " + sale.getSaleId(), e);
            }
        }

        table.setItems(salesObservable);
        return sales;
    }
}