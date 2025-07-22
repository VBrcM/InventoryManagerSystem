package Dialogs;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import Pages.AccessPage;

public class DialogManager {
    // Displays the given dialog node inside an overlay with dimmed background
    public static void showDialog(Node dialogContent) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        StackPane dialogWrapper = new StackPane(dialogContent);
        dialogWrapper.setStyle("-fx-background-color: #2e2e2e; -fx-padding: 20; -fx-background-radius: 10;");
        dialogWrapper.setMaxWidth(500);
        dialogWrapper.setMaxHeight(400);

        overlay.getChildren().add(dialogWrapper);
        AccessPage.root.getChildren().add(overlay);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                AccessPage.root.getChildren().remove(overlay);
                System.out.println("Dialog closed by clicking outside");
            }
        });

        System.out.println("Dialog displayed");
    }
}