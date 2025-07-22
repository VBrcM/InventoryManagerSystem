package Dialogs;

import Pages.AccessPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class PopUpDialog {

    public static void showInfo(String message) {
        showOverlay("Info", message, false, null);
    }

    public static void showError(String message) {
        showOverlay("Error", message, false, null);
    }

    public static void showConfirmation(String title, String message, Runnable onConfirm) {
        showOverlay(title, message, true, onConfirm);
    }

    private static void showOverlay(String titleText, String message, boolean isConfirm, Runnable onConfirm) {
        StackPane root = AccessPage.root;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        VBox dialogBox = new VBox(20);
        dialogBox.setPadding(new Insets(20));
        dialogBox.setAlignment(Pos.CENTER_LEFT);
        dialogBox.setMaxWidth(500);
        dialogBox.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 6;");
        dialogBox.setFillWidth(true); // prevent stretching
        dialogBox.setMaxHeight(Region.USE_PREF_SIZE); // Allow height to adjust to content

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Label body = new Label(message);
        body.setWrapText(true);
        body.setMaxWidth(460);
        body.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = new Button(isConfirm ? "Yes" : "OK");
        okButton.setStyle(buttonStyle("#666666"));
        okButton.setOnAction(e -> {
            root.getChildren().remove(overlay);
            if (onConfirm != null) onConfirm.run();
        });

        if (isConfirm) {
            Button cancelButton = new Button("No");
            cancelButton.setStyle(buttonStyle("#444444"));
            cancelButton.setOnAction(e -> root.getChildren().remove(overlay));
            buttonBox.getChildren().addAll(okButton, cancelButton);
        } else {
            buttonBox.getChildren().add(okButton);
        }

        dialogBox.getChildren().addAll(title, body, buttonBox);

        StackPane dialogWrapper = new StackPane(dialogBox);
        dialogWrapper.setStyle("-fx-padding: 20;");
        overlay.getChildren().add(dialogWrapper);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                root.getChildren().remove(overlay);
            }
        });

        root.getChildren().add(overlay);
    }

    private static String buttonStyle(String color) {
        return String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 10 20;",
                color
        );
    }
}