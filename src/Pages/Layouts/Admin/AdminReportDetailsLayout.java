package Pages.Layouts.Admin;


import DB.*;
import Model.DAO.SaleItemDAO;
import Model.POJO.SaleItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class AdminReportDetailsLayout {


    /**
     * Builds the report details page for a specific date.
     *
     * @param parentLayout The main layout to return to when "Back" is clicked
     * @param date         The date of the report
     * @return VBox containing the report details layout
     */
    public static VBox build(BorderPane parentLayout, LocalDate date) {
        // ===== Title =====
        Label title = new Label("Report Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(0, 0, 10, 0));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER_LEFT);


        // ===== Table Setup =====
        TableView<SaleItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");


        // ===== Table Columns =====
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
                setText(empty || val == null ? null : Formatter.formatNumber(val));
            }
        });


        TableColumn<SaleItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("siPrice"));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : Formatter.formatCurrency(val));
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
                setText(empty || val == null ? null : Formatter.formatCurrency(val));
            }
        });


        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol, totalCol);
        try {
            table.getItems().setAll(SaleItemDAO.getSaleItemsByDate(date));
        } catch (SQLException e) {
            e.printStackTrace(); // Log to console (for dev)
        }
        double grandTotal = table.getItems().stream()
                .mapToDouble(item -> item.getSiQty() * item.getSiPrice())
                .sum();

        Label totalLabel = new Label("Total Amount: " + Formatter.formatCurrency(grandTotal));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox totalBox = new HBox(totalLabel);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox.setVgrow(table, Priority.ALWAYS);


        // ===== Back Button =====
        Button backBtn = new Button("Back to Reports");
        backBtn.getStyleClass().add("inventory-button");
        backBtn.setPrefSize(200, 50);
        backBtn.setOnAction(e -> parentLayout.setCenter(AdminReportsLayout.build(parentLayout)));


        HBox buttonBox = new HBox(backBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));


        // ===== Final Layout =====
        VBox layout = new VBox(20, title, table, totalBox, buttonBox);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);


        return layout;
    }
}