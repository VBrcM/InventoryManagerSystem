package Pages.Layouts;

import DB.*;
import Model.DAO.SaleItemDAO;
import Model.POJO.SaleItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminReportDetailsLayout {

    public static VBox build(BorderPane parentLayout, LocalDate date, List<SaleItem> transactions) {
        // Title
        Label title = new Label("Report Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(0, 0, 10, 0));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER_LEFT);

        // Table
        TableView<SaleItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        // Columns
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
        table.getItems().addAll(SaleItemDAO.getSaleItemsByDate(date));

        // Back Button
        Button backBtn = new Button("Back to Reports");
        backBtn.getStyleClass().add("inventory-button");
        backBtn.setPrefHeight(50);
        backBtn.setPrefWidth(200);
        backBtn.setOnAction(e -> parentLayout.setCenter(AdminReportsLayout.build(parentLayout)));

        HBox buttonBox = new HBox(backBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // Final layout
        VBox layout = new VBox(20, title, table, buttonBox);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }
}