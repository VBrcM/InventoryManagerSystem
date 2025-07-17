package Pages.Layouts;

import Dialogs.InventoryDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminInventoryLayout {

    public static StackPane build() {
        // ===== Title =====
        Label title = new Label("Inventory");
        title.setId("title-label");

        // ===== Search Field =====
        TextField searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.getStyleClass().add("input-field");

        // ===== TableView =====
        TableView<String> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<String, String> nameCol = new TableColumn<>("Item Name");
        TableColumn<String, String> categoryCol = new TableColumn<>("Category");
        TableColumn<String, String> quantityCol = new TableColumn<>("Quantity");
        TableColumn<String, String> priceCol = new TableColumn<>("Price");

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ===== Action Buttons =====
        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        addBtn.getStyleClass().add("inventory-button");
        editBtn.getStyleClass().add("inventory-button");
        deleteBtn.getStyleClass().add("inventory-button");

        HBox actionButtons = new HBox(20, addBtn, editBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));

        // ===== Main Layout =====
        VBox content = new VBox(20, title, searchField, table, actionButtons);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1e1e1e;");

        // ===== Root StackPane for overlay =====
        StackPane root = new StackPane(content);

        // ===== Dialog Trigger =====
        addBtn.setOnAction(e -> InventoryDialog.show());

        return root;
    }
}
