package Dialogs;

import Model.DAO.CategoryDAO;
import Model.DAO.ProductDAO;
import Model.POJO.Category;
import Model.POJO.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Displays a modal dialog to add or edit inventory products.
 * Supports live filtering of categories in a styled ComboBox.
 * Updates the product list after insert or update.
 */
public class InventoryDialog {
    /**
     * Launches the inventory form dialog.
     * If a product is provided, pre-populates fields for editing.
     * Otherwise, initializes empty fields for adding a new item.
     * The provided product list is updated based on changes.
     * A callback is triggered after successful save.
     */
    public static void show(Product productToEdit, ObservableList<Product> products, Runnable onUpdated) {
        VBox dialogBox = new VBox(16);
        dialogBox.getStyleClass().add("inventory-dialog");
        dialogBox.setPadding(new Insets(20));
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxWidth(520);
        dialogBox.setPrefWidth(480);

        Label title = new Label(productToEdit == null ? "Add Inventory Item" : "Edit Inventory Item");
        title.getStyleClass().add("dialog-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        nameField.setPrefSize(400, 36);
        nameField.getStyleClass().add("dialog-pane");

        AtomicBoolean isInitializing;
        isInitializing = new AtomicBoolean(true);

        CategoryDAO categoryDAO = new CategoryDAO();
        ObservableList<String> categoryNames = FXCollections.observableArrayList(CategoryDAO.getAllCategoryNames());

        ComboBox<String> categoryField = new ComboBox<>(categoryNames);
        categoryField.setEditable(true);
        categoryField.setPromptText("Category");
        categoryField.setPrefSize(300, 36);
        categoryField.getStyleClass().add("text-like-combo");
        categoryField.setVisibleRowCount(6);

        FilteredList<String> filtered = new FilteredList<>(categoryNames, s -> true);
        categoryField.setItems(filtered);
        categoryField.setEditable(true);
        categoryField.getSelectionModel().clearSelection();

        filtered.addListener((ListChangeListener<String>) change -> {
            categoryField.setVisibleRowCount(Math.min(filtered.size(), 6));
        });

        categoryField.setOnHidden(e -> categoryField.getSelectionModel().clearSelection());

        categoryField.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            String input = newText == null ? "" : newText;
            filtered.setPredicate(item -> item.toLowerCase().contains(input.toLowerCase()));
            categoryField.setVisibleRowCount(Math.min(filtered.size(), 6));

            if (!input.trim().isEmpty() && !filtered.isEmpty()) {
                if (!categoryField.isShowing()) {
                    categoryField.show();
                }

                Platform.runLater(() -> {
                    if (categoryField.isShowing()) {
                        categoryField.hide();
                        categoryField.show();
                    }
                });
            } else {
                categoryField.hide();
            }
        });

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefSize(190, 36);
        priceField.getStyleClass().add("dialog-pane");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setPrefSize(190, 36);
        quantityField.getStyleClass().add("dialog-pane");

        HBox rowInputs = new HBox(10, categoryField, priceField, quantityField);
        rowInputs.setAlignment(Pos.CENTER);

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setWrapText(true);
        descriptionField.setMinHeight(100);
        descriptionField.setMaxHeight(100);
        descriptionField.setPrefWidth(400);
        descriptionField.getStyleClass().add("desc-area");

        descriptionField.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin instanceof javafx.scene.control.skin.TextAreaSkin) {
                ScrollPane scrollPane = (ScrollPane) descriptionField.lookup(".scroll-pane");
                if (scrollPane != null) {
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                }
            }
        });

        if (productToEdit != null) {
            nameField.setText(productToEdit.getProductName());
            priceField.setText(String.valueOf(productToEdit.getProductPrice()));
            quantityField.setText(String.valueOf(productToEdit.getStock()));
            descriptionField.setText(productToEdit.getDescription());

            Platform.runLater(() -> {
                categoryField.setValue(productToEdit.getCategoryName());
                isInitializing.set(false);
            });
        } else {
            isInitializing.set(false);
        }

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.getStyleClass().add("inventory-button");
        cancelButton.getStyleClass().add("inventory-button");

        HBox buttonBox = new HBox(20, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        dialogBox.getChildren().addAll(title, nameField, rowInputs, descriptionField, buttonBox);

        saveButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String categoryName = categoryField.getEditor().getText().trim();
                String priceText = priceField.getText().trim();
                String quantityText = quantityField.getText().trim();
                String description = descriptionField.getText().trim();

                if (name.isEmpty() || categoryName.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
                    PopUpDialog.showError("Please fill in all fields.");
                    return;
                }

                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(quantityText);

                Category categoryObj = categoryDAO.getOrCreateCategoryByName(categoryName);
                if (categoryObj == null) {
                    PopUpDialog.showError("Category could not be created.");
                    return;
                }

                Product product = new Product();
                product.setProductId(productToEdit != null ? productToEdit.getProductId() : 0);
                product.setProductName(name);
                product.setCategoryId(categoryObj.getCategoryId());
                product.setCategoryName(categoryObj.getCategoryName());
                product.setProductPrice(price);
                product.setStock(quantity);
                product.setDescription(description);

                if (productToEdit == null) {
                    Product inserted = ProductDAO.insert(product);
                    inserted.setCategoryName(categoryObj.getCategoryName());
                    boolean add = products.add(inserted);
                    categoryNames.setAll(CategoryDAO.getAllCategoryNames());
                } else {
                    ProductDAO.update(product);
                    int index = products.indexOf(productToEdit);
                    if (index != -1) products.set(index, product);
                }

                DialogManager.closeDialog();
                onUpdated.run();

            } catch (NumberFormatException ex) {
                PopUpDialog.showError("Invalid number format.");
            } catch (Exception ex) {
                PopUpDialog.showError("An unexpected error occurred.");
            }
        });

        cancelButton.setOnAction(e -> DialogManager.closeDialog());

        DialogManager.showDialog(dialogBox, false);
    }
}