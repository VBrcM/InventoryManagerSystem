package Pages.Layouts;

import DB.*;
import Dialogs.InventoryDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

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
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Product, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("product"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        //POPULATE TABLE
        ProductDAO dao = new ProductDAO();
        List<Product> productList = dao.getAllWithCategory();
        ObservableList<Product> products = FXCollections.observableArrayList(productList);
        table.setItems(products);


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
        addBtn.setOnAction(e -> InventoryDialog.show(null, products));

        return root;
    }
}
