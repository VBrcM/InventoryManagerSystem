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

    // ======================
    // === PUBLIC METHODS ===
    // ======================

    public static void showInfo(String message) {
        showOverlay("Information", message, false, null);
    }

    public static void showError(String message) {
        showOverlay("Error", message, false, null);
    }

    public static void showConfirmation(String title, String message, Runnable onConfirm) {
        showOverlay(title, message, true, onConfirm);
    }

    // ======================
    // === CORE OVERLAY UI ==
    // ======================

    private static void showOverlay(String titleText, String message, boolean isConfirm, Runnable onConfirm) {
        StackPane root = AccessPage.root;

        // === Dim Background Overlay ===
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());
        overlay.setOnMouseClicked(MouseEvent::consume);

        // === Dialog Box ===
        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(30));
        dialogBox.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 12;");
        dialogBox.maxWidthProperty().bind(root.widthProperty().multiply(0.3));
        dialogBox.maxHeightProperty().bind(root.heightProperty().multiply(0.3));

        // === Title Label ===
        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // === Message Label ===
        Label body = new Label(message);
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
        body.setMaxWidth(Double.MAX_VALUE);
        body.setAlignment(Pos.CENTER);
        body.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

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

        // === Final Assembly ===
        dialogBox.getChildren().addAll(title, body, buttons);
        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);
        root.getChildren().add(overlay);
    }
}
