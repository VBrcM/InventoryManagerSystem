package Dialogs;

import Pages.AccessPage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import DB.*;
import java.util.Optional;

public class InventoryDialog {
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void show(Product productToEdit, ObservableList<Product> products) {
        StackPane root = AccessPage.root;

        // Overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.setPadding(new Insets(300));

        // Dialog container
        VBox dialogBox = new VBox(18);
        dialogBox.setPadding(new Insets(10));
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 12;");
        dialogBox.setMaxWidth(600);
        dialogBox.setPrefWidth(400);
        dialogBox.setPrefHeight(300);

        Label title = new Label("Add Inventory Item");
        title.getStyleClass().add("dialog-title");

        double rowWidth = dialogBox.getLayoutBounds().getWidth();
        double rowHeight = 36;

        //ITEM
        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        nameField.setPrefSize(rowWidth, rowHeight);
        nameField.getStyleClass().add("dialog-pane");

        //CATEGORY
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        categoryField.setPrefSize(300, rowHeight);
        categoryField.getStyleClass().add("dialog-pane");

        //PRICE
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefSize(190, rowHeight);
        priceField.getStyleClass().add("dialog-pane");

        //QUANTITY
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setPrefSize(190, rowHeight);
        quantityField.getStyleClass().add("dialog-pane");

        HBox rowInputs = new HBox(rowWidth/3, categoryField, priceField, quantityField);
        rowInputs.setAlignment(Pos.CENTER);
        rowInputs.setSpacing(10);

        //DESCRIPTION
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefSize(rowWidth, rowHeight * 3);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("dialog-pane");

        //For Editing
        if (productToEdit != null) {
            nameField.setText(productToEdit.getProduct());
            categoryField.setText(productToEdit.getCategoryName());
            priceField.setText(String.valueOf(productToEdit.getPrice()));
            quantityField.setText(String.valueOf(productToEdit.getStock()));
            descriptionField.setText(productToEdit.getDescription());
        }

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.getStyleClass().add("inventory-button");
        cancelButton.getStyleClass().add("inventory-button");


        //SAVE BUTTON TO UI-TO-DB
        saveButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String categoryName = categoryField.getText();
                double price = Double.parseDouble(priceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                String description = descriptionField.getText();

                CategoryDAO categoryDAO = new CategoryDAO();
                Category categoryObj = categoryDAO.getOrCreateCategoryByName(categoryName);

                Product product = new Product();
                product.setProductId(productToEdit != null ? productToEdit.getProductId() : 0);
                product.setProduct(name);
                product.setCategoryId(categoryObj.getCategoryId()); // ✅ category ID is now set
                product.setPrice(price);
                product.setStock(quantity);
                product.setDescription(description);

                ProductDAO dao = new ProductDAO();
                if (productToEdit == null) {
                    Product inserted = dao.insert(product); // returns inserted product with ID
                    products.add(inserted); // ✅ add to ObservableList
                } else {
                    dao.update(product);
                    int index = products.indexOf(productToEdit);
                    if (index != -1) {
                        products.set(index, product); // ✅ update ObservableList
                    }
                }

                root.getChildren().remove(overlay);
            } catch (Exception ex) {
                showError("Invalid input. Please check the fields.");
            }
        });
        cancelButton.setOnAction(e -> root.getChildren().remove(overlay));

        HBox buttonBox = new HBox(20, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        dialogBox.getChildren().addAll(
                title,
                nameField,
                rowInputs,
                descriptionField,
                buttonBox
        );

        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);
        root.getChildren().add(overlay);
    }
}
