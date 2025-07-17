package Pages;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AccessLayout {

    // ===== UI Initialization and Styling =====
    public static void show() {
        Label label = new Label("Enter Access Code:");

        PasswordField accessCodeField = new PasswordField();
        accessCodeField.setPromptText("Access Code");
        accessCodeField.getStyleClass().add("input-field");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

        Button submitButton = new Button("Submit");
        submitButton.setDefaultButton(true);
        submitButton.getStyleClass().add("submit-button");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("exit-button");

        // ===== Submit Button Logic =====
        submitButton.setOnAction(e -> {
            String code = accessCodeField.getText();
            Stage currentStage = (Stage) AccessPage.root.getScene().getWindow();

            if (code.equals("12345")) {
                errorLabel.setText("");
                EmployeeAccess.show();
            } else if (code.equals("admin123")) {
                errorLabel.setText("");
                AdminAccess.show(currentStage);
            } else {
                errorLabel.setText("Invalid Access Code");
            }
        });

        // ===== Exit Button Logic =====
        exitButton.setOnAction(e -> Platform.exit());

        // ===== Layout Setup =====
        VBox layout = new VBox(10, label, accessCodeField, submitButton, exitButton, errorLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        VBox.setVgrow(accessCodeField, Priority.ALWAYS);
        VBox.setVgrow(submitButton, Priority.ALWAYS);
        VBox.setVgrow(exitButton, Priority.ALWAYS);

        // ===== Scene Display =====
        AccessPage.root.getChildren().setAll(layout);
    }
}
