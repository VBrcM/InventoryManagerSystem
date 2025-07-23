package Dialogs;

import DB.*;
import Model.DAO.SaleDAO;
import Model.DAO.SaleItemDAO;
import Model.DAO.TransactionDAO;
import Model.POJO.Product;
import Model.POJO.SaleItem;
import Model.POJO.Transaction;
import Pages.AccessPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
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
        Label nameLabel = new Label("Product: " + selectedProduct.getProductName());
        Label categoryLabel = new Label("Category: " + selectedProduct.getCategoryName());
        Label currentStockLabel = new Label("Current Stock: " + selectedProduct.getStock());
        Label priceLabel = new Label("Unit Price: " + Formatter.formatCurrency(selectedProduct.getProductPrice()));

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

            // Prevent reducing more than stock
            if (type.equals("reduce") && quantity > selectedProduct.getStock()) {
                PopUpDialog.showError("Cannot reduce more than available stock.");
                return;
            }

            try {
                // Step 1: Record Transaction
                Transaction txn = new Transaction();
                txn.setType(type.toUpperCase());
                txn.setProductId(selectedProduct.getProductId());
                txn.setQuantity(quantity);

                boolean txnSuccess = TransactionDAO.recordTransaction(txn);
                if (!txnSuccess) {
                    PopUpDialog.showError("Transaction failed.");
                    return;
                }

                // Step 2: Record Sale and Sale Item if reducing stock
                if (type.equals("reduce")) {
                    int saleId = SaleDAO.insert(quantity, selectedProduct.getProductPrice() * quantity);

                    SaleItem item = new SaleItem();
                    item.setSaleId(saleId);
                    item.setProductId(selectedProduct.getProductId());
                    item.setSiQty(quantity);
                    item.setSiPrice(selectedProduct.getProductPrice());
                    item.setSiDate(LocalDate.now());

                    boolean itemSuccess = SaleItemDAO.insertSaleItem(item);
                    if (!itemSuccess) {
                        PopUpDialog.showError("Sale recorded but failed to add sale item.");
                        return;
                    }
                }

                // Success
                PopUpDialog.showInfo("Transaction recorded successfully.");
                onSaved.accept(quantity);
                root.getChildren().remove(overlay);

            } catch (Exception ex) {
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
