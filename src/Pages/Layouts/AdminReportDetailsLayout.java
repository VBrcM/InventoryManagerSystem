package Pages.Layouts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminReportDetailsLayout {

    public static VBox build(BorderPane parentLayout, LocalDate date, List<SoldItem> transactions) {

        // Section: Title
        Label title = new Label("Report Details - " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        title.setId("title-label");

        // Section: Table setup
        TableView<SoldItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<SoldItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<SoldItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());

        TableColumn<SoldItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());

        TableColumn<SoldItem, Integer> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> data.getValue().priceProperty().asObject());

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        table.getItems().addAll(transactions);

        // Section: Back button
        Button backBtn = new Button("Back to Reports");
        backBtn.setOnAction(e -> parentLayout.setCenter(AdminReportsLayout.build(parentLayout)));
        backBtn.getStyleClass().add("inventory-button");

        // Section: Layout container
        VBox layout = new VBox(20, title, table, backBtn);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(table, Priority.ALWAYS);

        return layout;
    }
}
