package Dialogs;

import Pages.AccessPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PopUpDialog {

    public static void showInfo(String message) {
        showOverlay("Information", message, false, null);
    }

    public static void showError(String message) {
        showOverlay("Error", message, false, null);
    }

    public static void showConfirmation(String title, String message, Runnable onConfirm) {
        showOverlay(title, message, true, onConfirm);
    }

    private static void showOverlay(String titleText, String message, boolean isConfirm, Runnable onConfirm) {
        StackPane root = AccessPage.root; // Assuming AccessPage.root is your main StackPane

        // === Dim Background Overlay ===
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());
        overlay.setOnMouseClicked(MouseEvent::consume); // Block background clicks

        // === Dialog Box ===
        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(30));
        dialogBox.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 12;");
        dialogBox.setMaxWidth(500);

        // === Title Label ===
        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        // === Message Label ===
        Label body = new Label(message);
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
        body.setMaxWidth(400);

        // === Buttons ===
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button okButton = new Button(isConfirm ? "Yes" : "OK");
        okButton.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16;");
        okButton.setOnAction(e -> {
            root.getChildren().remove(overlay);
            if (onConfirm != null) onConfirm.run();
        });

        if (isConfirm) {
            Button cancelButton = new Button("No");
            cancelButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16;");
            cancelButton.setOnAction(e -> root.getChildren().remove(overlay));
            buttons.getChildren().addAll(okButton, cancelButton);
        } else {
            buttons.getChildren().add(okButton);
        }

        dialogBox.getChildren().addAll(title, body, buttons);
        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);

        root.getChildren().add(overlay);
    }
}