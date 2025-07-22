package Dialogs;

import DB.*;
import Pages.AccessPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionDialog {

    public static void show(String type, Product selectedProduct, Consumer<Integer> onSaved) {
        StackPane root = AccessPage.root;

        // Create overlay to dim background
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.setPadding(new Insets(300));

        // Main dialog container
        VBox dialogBox = new VBox();
        dialogBox.setPadding(new Insets(25));
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setSpacing(20);
        dialogBox.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 12;");
        dialogBox.setMaxWidth(400);
        dialogBox.setPrefWidth(350);

        // Title
        Label title = new Label((type.equals("add") ? "Add" : "Reduce") + " Stock");
        title.getStyleClass().add("dialog-title");

        // Product info labels
        Label nameLabel = new Label("Product: " + selectedProduct.getProduct());
        Label categoryLabel = new Label("Category: " + selectedProduct.getCategoryName());
        Label currentStockLabel = new Label("Current Stock: " + selectedProduct.getStock());
        Label priceLabel = new Label("Unit Price: ₱" + String.format("%.2f", selectedProduct.getPrice()));

        String nameStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold";
        String infoStyle = "-fx-font-size: 14px; -fx-text-fill: white;";
        nameLabel.setStyle(nameStyle);
        categoryLabel.setStyle(infoStyle);
        currentStockLabel.setStyle(infoStyle);
        priceLabel.setStyle(infoStyle);

        VBox infoBox = new VBox(6, nameLabel, categoryLabel, currentStockLabel, priceLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Quantity input field
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        quantityField.setPrefWidth(200);
        quantityField.getStyleClass().add("input-field");

        // Buttons
        Button confirmBtn = new Button("Confirm");
        Button cancelBtn = new Button("Cancel");
        confirmBtn.getStyleClass().add("inventory-button");
        cancelBtn.getStyleClass().add("inventory-button");

        // Confirm logic
        confirmBtn.setOnAction(e -> {
            String qtyText = quantityField.getText().trim();

            if (qtyText.isEmpty() || !qtyText.matches("\\d+")) {
                PopUpDialog.showError("Please enter a valid quantity.");
                return;
            }

            int quantity = Integer.parseInt(qtyText);

            if (type.equals("reduce") && quantity > selectedProduct.getStock()) {
                PopUpDialog.showError("Cannot reduce more than available stock.");
                return;
            }

            try {
                System.out.println("[DEBUG] Transaction type passed in: " + type);
                Transaction txn = new Transaction();
                txn.setType(type.toUpperCase()); // ✅ FIXED: convert to uppercase
                txn.setProductId(selectedProduct.getProductId());
                txn.setQuantity(quantity);

                boolean success = TransactionDAO.recordTransaction(txn);

                if (success) {
                    PopUpDialog.showInfo("Transaction recorded successfully.");
                    onSaved.accept(quantity);
                    root.getChildren().remove(overlay);
                } else {
                    PopUpDialog.showError("Transaction failed.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                PopUpDialog.showError("A database error occurred: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> root.getChildren().remove(overlay));

        HBox buttons = new HBox(20, confirmBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        // Add all to dialog
        dialogBox.getChildren().addAll(title, infoBox, quantityField, buttons);

        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);

        // Show overlay
        root.getChildren().add(overlay);
    }
}
