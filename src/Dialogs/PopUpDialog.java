package Dialogs;

import Pages.AccessPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Utility class for displaying popup dialogs including success, error, and confirmation messages.
 * All dialogs are modal and use styled overlays injected into the AccessPage root.
 */
public class PopUpDialog {

    private static final Logger logger = Logger.getLogger(PopUpDialog.class.getName());

    /**
     * Displays a modal error dialog with a dismiss button.
     * Used for critical failures or invalid actions.
     */
    public static void showError(String message) {
        show(message, "Error");
    }

    /**
     * Displays a modal success dialog with a dismiss button.
     * Used after successful operations such as saving or completing a transaction.
     */
    public static void showSuccess(String message) {
        show(message, "Success");
    }

    /**
     * Displays a modal confirmation dialog with Yes and No buttons.
     * The result of the user action is returned via the callback.
     */
    public static void showConfirmation(String title, String message, Consumer<Boolean> onResult) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox dialogBox = new VBox(18);
        dialogBox.getStyleClass().add("inventory-dialog");
        dialogBox.setMaxWidth(420);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("popup-title-danger");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("popup-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        Button yesBtn = new Button("Yes");
        Button noBtn = new Button("No");
        yesBtn.getStyleClass().add("popup-button");
        noBtn.getStyleClass().add("popup-button");

        yesBtn.setOnAction(e -> {
            ((Pane) overlay.getParent()).getChildren().remove(overlay);
            onResult.accept(true);
        });

        noBtn.setOnAction(e -> {
            ((Pane) overlay.getParent()).getChildren().remove(overlay);
            onResult.accept(false);
        });

        HBox buttonBox = new HBox(20, yesBtn, noBtn);
        buttonBox.setAlignment(Pos.CENTER);

        dialogBox.getChildren().addAll(titleLabel, messageLabel, buttonBox);
        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);

        AccessPage.root.getChildren().add(overlay);
        logger.info("Confirmation dialog shown: " + message);
    }

    /**
     * Internal method to show either success or error message in a modal dialog.
     * Includes an OK button to dismiss the dialog.
     */
    private static void show(String message, String titleText) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox dialogBox = new VBox(16);
        dialogBox.getStyleClass().add("popup-dialog");
        dialogBox.setPadding(new Insets(20));
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxWidth(440);

        Label title = new Label(titleText);
        if (titleText.equalsIgnoreCase("Success")) {
            title.getStyleClass().add("popup-title-success");
        } else {
            title.getStyleClass().add("popup-title-danger");
        }

        Label msg = new Label(message);
        msg.getStyleClass().add("popup-message");
        msg.setWrapText(true);
        msg.setMaxWidth(400);

        Button closeBtn = new Button("OK");
        closeBtn.getStyleClass().add("popup-button");
        closeBtn.setOnAction(e -> {
            AccessPage.root.getChildren().remove(overlay);
        });

        dialogBox.getChildren().addAll(title, msg, closeBtn);
        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);

        AccessPage.root.getChildren().add(overlay);
        logger.info("Popup displayed: " + titleText + " - " + message);
    }
}