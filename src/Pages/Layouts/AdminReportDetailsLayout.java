package Pages.Layouts;

import DB.SaleItem;
import DB.SaleItemDAO;
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

        // Display report title with selected date
        Label title = new Label("Report Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");

        // Create table for sale items
        TableView<SaleItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        // Define table columns for sale item attributes
        TableColumn<SaleItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<SaleItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<SaleItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<SaleItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);

        List<SaleItem> items = SaleItemDAO.getSaleItemsByDate(date);
        for (SaleItem s : items) {
            System.out.println("Loaded Item: " + s.getProductName() + " | Qty: " + s.getQuantity());
        }
        table.getItems().addAll(items);

        // Create button to return to main report view
        Button backBtn = new Button("Back to Reports");
        backBtn.setOnAction(e -> parentLayout.setCenter(AdminReportsLayout.build(parentLayout)));
        backBtn.getStyleClass().add("inventory-button");

        // Layout containing title, table, and back button
        VBox layout = new VBox(20, title, table, backBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }
}
