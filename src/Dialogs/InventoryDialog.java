package Dialogs;

import Model.DAO.CategoryDAO;
import Model.DAO.ProductDAO;
import Model.POJO.Category;
import Model.POJO.Product;
import Pages.AccessPage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class InventoryDialog {
    public static void show(Product productToEdit, ObservableList<Product> products, Runnable onUpdated) {
        StackPane root = AccessPage.root;

        // Overlay to darken the background and focus user on dialog
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.setPadding(new Insets(300));

        // Dialog container with styling
        VBox dialogBox = new VBox(18);
        dialogBox.setPadding(new Insets(10));
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 12;");
        dialogBox.setMaxWidth(600);
        dialogBox.setPrefWidth(400);
        dialogBox.setPrefHeight(300);

        // Title of the dialog
        Label title = new Label("Add Inventory Item");
        title.getStyleClass().add("dialog-title");

        // Input fields for item details
        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        nameField.setPrefSize(400, 36);
        nameField.getStyleClass().add("dialog-pane");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        categoryField.setPrefSize(300, 36);
        categoryField.getStyleClass().add("dialog-pane");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefSize(190, 36);
        priceField.getStyleClass().add("dialog-pane");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setPrefSize(190, 36);
        quantityField.getStyleClass().add("dialog-pane");

        // Group price, category, and quantity inputs in a row
        HBox rowInputs = new HBox(10, categoryField, priceField, quantityField);
        rowInputs.setAlignment(Pos.CENTER);

        // Text area for optional product description
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefSize(400, 208);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("desc-area");

        // If editing, populate the fields with the existing product's values
        if (productToEdit != null) {
            nameField.setText(productToEdit.getProductName());
            categoryField.setText(productToEdit.getCategoryName());
            priceField.setText(String.valueOf(productToEdit.getProductPrice()));
            quantityField.setText(String.valueOf(productToEdit.getStock()));
            descriptionField.setText(productToEdit.getDescription());
        }

        // Buttons for saving and cancelling
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.getStyleClass().add("inventory-button");
        cancelButton.getStyleClass().add("inventory-button");

        // Handle Save action
        saveButton.setOnAction(e -> {
            try {
                // Read input values
                String name = nameField.getText();
                String categoryName = categoryField.getText();
                String priceText = priceField.getText();
                String quantityText = quantityField.getText();
                String description = descriptionField.getText();

                // Basic validation for required fields
                if (name.isEmpty() || categoryName.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
                    PopUpDialog.showError("Please fill in all fields.");
                    return;
                }

                // Convert price and quantity
                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(quantityText);

                // Find or create category by name
                CategoryDAO categoryDAO = new CategoryDAO();
                Category categoryObj = categoryDAO.getOrCreateCategoryByName(categoryName);

                // Prepare product object to insert or update
                Product product = new Product();
                product.setProductId(productToEdit != null ? productToEdit.getProductId() : 0);
                product.setProductName(name);
                product.setCategoryId(categoryObj.getCategoryId());
                product.setCategoryName(categoryObj.getCategoryName()); // for UI purposes
                product.setProductPrice(price);
                product.setStock(quantity);
                product.setDescription(description);

                ProductDAO dao = new ProductDAO();

                // Insert or update in DB
                if (productToEdit == null) {
                    // New product
                    Product inserted = dao.insert(product);
                    inserted.setCategoryName(categoryObj.getCategoryName());
                    products.add(inserted); // Add to ObservableList
                } else {
                    // Update existing product
                    dao.update(product);
                    int index = products.indexOf(productToEdit);
                    if (index != -1) {
                        products.set(index, product); // Replace updated product
                    }
                }

                // Close the dialog
                root.getChildren().remove(overlay);

            } catch (NumberFormatException ex) {
                PopUpDialog.showError("Invalid number format for price or quantity.");
            } catch (Exception ex) {
                PopUpDialog.showError("An error occurred. Please check your input.");
            }

            // Run optional callback for UI refresh
            onUpdated.run();
        });

        // Close dialog on cancel
        cancelButton.setOnAction(e -> root.getChildren().remove(overlay));

        // Layout buttons
        HBox buttonBox = new HBox(20, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Assemble the full dialog
        dialogBox.getChildren().addAll(
                title,
                nameField,
                rowInputs,
                descriptionField,
                buttonBox
        );

        // Add dialog to overlay
        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);

        // Display the overlay in the main root pane
        root.getChildren().add(overlay);
    }
}