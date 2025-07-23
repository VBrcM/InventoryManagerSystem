package Pages.Layouts;

import Model.DAO.SaleDAO;
import Model.DAO.SaleItemDAO;
import Model.POJO.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        loadTodaysTransactions(table);

        VBox layout = new VBox(15, title, dateLabel, table);
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
    private static void loadTodaysTransactions(TableView<Transaction> table) {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        List<Sale> sales = SaleDAO.getSalesByDate(LocalDate.now());

        for (Sale sale : sales) {
            String items = SaleItemDAO.getItemSummaryBySaleId(sale.getId());
            String time = sale.getDateTime() != null
                    ? sale.getDateTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
                    : "N/A";

            Transaction tx = new Transaction(time, items, sale.getTotalAmount());
            transactions.add(tx);
        }

        table.setItems(transactions);
    }

    // =================================
    // === INTERNAL DISPLAY MODEL CLASS
    // =================================
    public static class Transaction {
        private final String time;
        private final String itemsSummary;
        private final double total;

        public Transaction(String time, String itemsSummary, double total) {
            this.time = time;
            this.itemsSummary = itemsSummary;
            this.total = total;
        }

        public String getTime() {
            return time;
        }

        public String getItemsSummary() {
            return itemsSummary;
        }

        public double getTotal() {
            return total;
        }
    }
}
