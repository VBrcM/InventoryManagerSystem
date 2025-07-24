package Pages.Layouts.Employee;

import DB.Formatter;
import Model.DAO.SaleDAO;
import Model.DAO.SaleItemDAO;
import Model.POJO.Sale;
import Model.POJO.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeTransactionLayout {

    // =======================
    // === MAIN BUILD METHOD ==
    // =======================
    public static VBox build() {
        Label title = new Label("Today's Transactions");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");

        TableView<Transaction> table = createTransactionTable();
        List<Sale> sales = loadTodaysTransactions(table);

        // ===== Total Calculation =====
        double grandTotal = sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();

        Label totalLabel = new Label("Total Amount: " + Formatter.formatCurrency(grandTotal));
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15, title, dateLabel, table, totalBox); // ⬅ add totalBox here
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }

    // =============================
    // === TRANSACTION TABLE SETUP ==
    // =============================
    private static TableView<Transaction> createTransactionTable() {
        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Transaction, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(100);

        TableColumn<Transaction, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(new PropertyValueFactory<>("itemsSummary"));
        itemsCol.setPrefWidth(200);

        TableColumn<Transaction, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(100);

        table.getColumns().addAll(timeCol, itemsCol, totalCol);

        return table;
    }

    // ====================================
    // === LOAD TRANSACTIONS FROM DATABASE
    // ====================================
    private static List<Sale> loadTodaysTransactions(TableView<Transaction> table) {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        List<Sale> sales = SaleDAO.getSalesByDate(LocalDate.now());

        for (Sale sale : sales) {
            String items = SaleItemDAO.getItemSummaryBySaleId(sale.getSaleId());
            String time = sale.getSaleDate() != null
                    ? sale.getSaleDate().format(DateTimeFormatter.ofPattern("hh:mm a"))
                    : "N/A";

            Transaction tnx = new Transaction(time, items, sale.getTotalAmount());
            transactions.add(tnx);
        }

        table.setItems(transactions);
        return sales; // ⬅ return the sales list for total calculation
    }

}
