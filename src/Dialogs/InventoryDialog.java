package Dialogs;

import Pages.AccessPage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import DB.*;

public class InventoryDialog {
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
                String priceText = priceField.getText();
                String quantityText = quantityField.getText();
                String description = descriptionField.getText();

                if (name.isEmpty() || categoryName.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
                    PopUpDialog.showError("Please fill in all fields.");
                    return;
                }

                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(quantityText);

                CategoryDAO categoryDAO = new CategoryDAO();
                Category categoryObj = categoryDAO.getOrCreateCategoryByName(categoryName);

                Product product = new Product();
                product.setProductId(productToEdit != null ? productToEdit.getProductId() : 0);
                product.setProduct(name);
                product.setCategoryId(categoryObj.getCategoryId());
                product.setCategoryName(categoryObj.getCategory()); // Store for display
                product.setPrice(price);
                product.setStock(quantity);
                product.setDescription(description);

                ProductDAO dao = new ProductDAO();
                if (productToEdit == null) {
                    Product inserted = dao.insert(product);
                    inserted.setCategoryName(categoryObj.getCategory());
                    products.add(inserted);
                } else {
                    dao.update(product);
                    int index = products.indexOf(productToEdit);
                    if (index != -1) {
                        products.set(index, product);
                    }
                }

                root.getChildren().remove(overlay);
            } catch (NumberFormatException ex) {
                PopUpDialog.showError("Invalid number format for price or quantity.");
            } catch (Exception ex) {
                PopUpDialog.showError("An error occurred. Please check your input.");
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
