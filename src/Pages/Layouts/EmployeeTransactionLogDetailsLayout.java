package Pages.Layouts;

import DB.*;
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

        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.valueOf(cellData.getValue().getType())
        ));

        TableColumn<Transaction, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        qtyCol.setCellFactory(col -> new TableCell<Transaction, Integer>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(Formatter.formatNumber(value));
                }
            }
        });

        TableColumn<Transaction, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                Formatter.formatTime(cellData.getValue().getDate().atTime(0, 0))
        ));

        table.getColumns().addAll(productCol, typeCol, qtyCol, timeCol);
        table.getItems().addAll(logs);

        System.out.println("[DEBUG] Loaded " + logs.size() + " transactions into table.");

        Button backBtn = new Button("Back to Log");
        backBtn.setOnAction(e -> {
            System.out.println("[DEBUG] Back button clicked. Returning to transaction log.");
            parentLayout.setCenter(EmployeeTransactionLogLayout.build(parentLayout));
        });
        backBtn.getStyleClass().add("inventory-button");

        VBox layout = new VBox(20, title, table, backBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }
}

