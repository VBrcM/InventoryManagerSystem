package Pages.Layouts.Employee;

import DB.Formatter;
import Dialogs.*;
import Model.DAO.*;
import Model.POJO.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeTransactionLogDetailsLayout {

    public static VBox build(BorderPane parentLayout, LocalDate date, List<Sale> sales){
        System.out.println("[DEBUG] Building Transaction Details Layout for date: " + date);

        Label title = new Label("Transaction Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");

        // ===== Table Setup (Using Sale) =====
        TableView<Sale> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

        // ===== Time Column =====
        TableColumn<Sale, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(Formatter.formatTime(cellData.getValue().getSaleDate()))
        );
        timeCol.setMaxWidth(1f * Integer.MAX_VALUE * 20);

        // ===== Items Column =====
        TableColumn<Sale, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> {
            List<String> itemStrings = new ArrayList<>();
            for (SaleItem item : cellData.getValue().getSaleItems()) {
                itemStrings.add(item.getProduct().getProductName() + " x" + item.getSiQty());
            }
            return new SimpleStringProperty(String.join(", ", itemStrings));
        });
        itemsCol.setMaxWidth(1f * Integer.MAX_VALUE * 60);

        // ===== Total Column =====
        TableColumn<Sale, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(Formatter.formatCurrency(cellData.getValue().getTotalAmount()))
        );
        totalCol.setMaxWidth(1f * Integer.MAX_VALUE * 20);

        table.getColumns().addAll(timeCol, itemsCol, totalCol);
        table.getItems().addAll(sales);

        // ===== Calculate Grand Total =====
        double grandTotal = sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();

        Label totalLabel = new Label("Total Amount: " + Formatter.formatCurrency(grandTotal));
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);


        // ===== Back Button =====
        Button backBtn = new Button("Back to Log");
        backBtn.setOnAction(e -> {
            System.out.println("[DEBUG] Back button clicked. Returning to transaction log.");
            parentLayout.setCenter(EmployeeTransactionLogLayout.build(parentLayout));
        });
        backBtn.getStyleClass().add("inventory-button");

        // ===== Layout Construction =====
        VBox layout = new VBox(20, title, table, totalBox, backBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }

    private static void showSaleDetails(Sale sale) {
        List<SaleItem> items = SaleItemDAO.getSaleItemsBySaleId(sale.getSaleId());

        int itemCount = items.stream().mapToInt(SaleItem::getSiQty).sum();
        ReceiptDialog.showContent(sale.getSaleDate(), items, itemCount);
    }

}