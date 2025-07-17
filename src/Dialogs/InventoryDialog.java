package Dialogs;

import Pages.AccessPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.awt.*;

public class InventoryDialog {

    public static void show() {
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

        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        nameField.setPrefSize(rowWidth, rowHeight);
        nameField.getStyleClass().add("dialog-pane");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        categoryField.setPrefSize(300, rowHeight);
        categoryField.getStyleClass().add("dialog-pane");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefSize(190, rowHeight);
        priceField.getStyleClass().add("dialog-pane");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setPrefSize(190, rowHeight);
        quantityField.getStyleClass().add("dialog-pane");

        HBox rowInputs = new HBox(rowWidth/3, categoryField, priceField, quantityField);
        rowInputs.setAlignment(Pos.CENTER);
        rowInputs.setSpacing(10);

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefSize(rowWidth, rowHeight * 3);
        descriptionField.setWrapText(true);
        descriptionField.getStyleClass().add("dialog-pane");


        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.getStyleClass().add("inventory-button");
        cancelButton.getStyleClass().add("inventory-button");

        saveButton.setOnAction(e -> root.getChildren().remove(overlay));
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
