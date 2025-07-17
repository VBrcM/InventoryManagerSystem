package Dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import Pages.AccessPage;

public class DialogManager {

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
            }
        });
    }

    public static VBox createSampleDialog() {
        VBox dialogContent = new VBox(15);
        dialogContent.setAlignment(Pos.CENTER_LEFT);
        dialogContent.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        TextField descField = new TextField();
        descField.setPromptText("Description");

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            AccessPage.root.getChildren().remove(AccessPage.root.getChildren().size() - 1);
        });

        dialogContent.getChildren().addAll(
                new Label("Item Name"), nameField,
                new Label("Quantity"), qtyField,
                new Label("Description"), descField,
                closeButton
        );

        return dialogContent;
    }
}
