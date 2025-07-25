package Dialogs;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import Pages.AccessPage;

/**
 * Handles the display and closing of modal dialogs with a dimmed background overlay.
 * <p>
 * Injects a dialog overlay into the main UI root defined in AccessPage.
 * Only one dialog can be shown at a time. Optionally supports dismissing the dialog
 * by clicking outside its content area.
 */
public class DialogManager {

    private static StackPane currentOverlay;

    /**
     * Shows a dialog with dimmed background.
     * Clicking outside the dialog will not close it.
     * <p>
     * Use this for critical or confirm-type dialogs that require explicit dismissal.
     */
    public static void showDialog(Node dialogContent) {
        showDialog(dialogContent, false);
    }

    /**
     * Shows a dialog with dimmed background and optional outside-click-to-close behavior.
     * If a dialog is already visible, it is closed before showing the new one.
     * <p>
     * If closeOnOutsideClick is true, clicking the dimmed area will close the dialog.
     * Otherwise, the dialog must be closed manually or through UI controls.
     */
    public static void showDialog(Node dialogContent, boolean closeOnOutsideClick) {
        closeDialog();

        currentOverlay = new StackPane();
        currentOverlay.getStyleClass().add("dialog-overlay");

        StackPane dialogWrapper = new StackPane(dialogContent);
        dialogWrapper.setMaxWidth(700);
        dialogWrapper.setMaxHeight(1000);

        currentOverlay.getChildren().add(dialogWrapper);
        AccessPage.root.getChildren().add(currentOverlay);

        if (closeOnOutsideClick) {
            currentOverlay.setOnMouseClicked(e -> {
                if (e.getTarget() == currentOverlay) {
                    closeDialog();
                    System.out.println("Dialog closed by clicking outside");
                }
            });
        } else {
            currentOverlay.setOnMouseClicked(e -> e.consume());
        }

        dialogWrapper.setOnMouseClicked(e -> e.consume());
        System.out.println("Dialog displayed");
    }

    /**
     * Closes the currently active dialog if one exists.
     * <p>
     * Removes the overlay from the root and clears its reference.
     */
    public static void closeDialog() {
        if (currentOverlay != null) {
            AccessPage.root.getChildren().remove(currentOverlay);
            currentOverlay = null;
            System.out.println("Dialog closed programmatically");
        }
    }
}