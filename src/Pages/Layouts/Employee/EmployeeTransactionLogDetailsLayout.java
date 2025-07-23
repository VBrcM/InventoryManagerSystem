package Pages.Layouts.Employee;

import DB.*;
import Dialogs.PopUpDialog;
import Model.DAO.TransactionDAO;
import Model.POJO.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeTransactionLogDetailsLayout {

    public static VBox build(BorderPane parentLayout, LocalDate date, List<Transaction> logs) {
        System.out.println("[DEBUG] Building Transaction Details Layout for date: " + date);

        Label title = new Label("Transaction Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");

        // Table setup
        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Transaction txn = row.getItem();
                    showTransactionDetails(txn);
                }
            });
            return row;
        });

        // ===== Time Column =====
        TableColumn<Transaction, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(Formatter.formatTime(cellData.getValue().getTDate()))
        );
        timeCol.setPrefWidth(100);

        // ===== Items Column =====
        TableColumn<Transaction, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getProductName() + " x" + cellData.getValue().getTQty()
                )
        );
        itemsCol.setPrefWidth(200);

        // ===== Total Column =====
        TableColumn<Transaction, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        Formatter.formatCurrency(cellData.getValue().getAmount())
                )
        );
        totalCol.setPrefWidth(100);

        // Add columns to table
        table.getColumns().addAll(timeCol, itemsCol, totalCol);
        table.getItems().addAll(logs);

        System.out.println("[DEBUG] Loaded " + logs.size() + " transactions into table.");

        // ===== Back Button =====
        Button backBtn = new Button("Back to Log");
        backBtn.setOnAction(e -> {
            System.out.println("[DEBUG] Back button clicked. Returning to transaction log.");
            parentLayout.setCenter(EmployeeTransactionLogLayout.build(parentLayout));
        });
        backBtn.getStyleClass().add("inventory-button");

        // ===== Layout Construction =====
        VBox layout = new VBox(20, title, table, backBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }
    private static void showTransactionDetails(Transaction txn) {
        List<Transaction> items = TransactionDAO.getTransactionsByTID(txn.getTId());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label timeLabel = new Label("Time: " + Formatter.formatTime(txn.getTDate()));
        timeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // ====== Dynamic Row of Products (Product(qty)) ======
        FlowPane itemsRow = new FlowPane();
        itemsRow.setHgap(12);
        itemsRow.setVgap(10);
        itemsRow.setPrefWrapLength(300); // auto wrap
        itemsRow.setPadding(new Insets(10));

        for (Transaction item : items) {
            String text = String.format("%s (x%d)",
                    item.getProduct().getProductName(), item.getTQty());

            Label label = new Label(text);
            label.setStyle("""
                -fx-background-color: #E0E0E0;
                -fx-padding: 6 10;
                -fx-border-radius: 5;
                -fx-background-radius: 5;
                -fx-font-size: 13;
        """);
            itemsRow.getChildren().add(label);
        }

        // ====== Table View for More Details ======
        GridPane table = new GridPane();
        table.setHgap(20);
        table.setVgap(10);
        table.setPadding(new Insets(5));

        table.add(new Label("Product"), 0, 0);
        table.add(new Label("Qty"), 1, 0);
        table.add(new Label("Price"), 2, 0);
        table.add(new Label("Subtotal"), 3, 0);

        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            Transaction item = items.get(i);
            double price = item.getProduct().getProductPrice();
            double subtotal = price * item.getTQty();
            total += subtotal;

            table.add(new Label(item.getProduct().getProductName()), 0, i + 1);
            table.add(new Label(String.valueOf(item.getTQty())), 1, i + 1);
            table.add(new Label(Formatter.formatCurrency(price)), 2, i + 1);
            table.add(new Label(Formatter.formatCurrency(subtotal)), 3, i + 1);
        }

        Label totalLabel = new Label("Total: " + Formatter.formatCurrency(total));
        totalLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        content.getChildren().addAll(timeLabel, itemsRow, table, totalLabel);

        PopUpDialog.showInfo("Transaction Info");
    }

}