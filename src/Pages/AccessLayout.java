package Pages;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AccessLayout {

    public static void show() {
        // --- Create UI Elements ---

        // Access code label
        Label label = new Label("Enter Access Code:");

        // Password input field
        PasswordField accessCodeField = new PasswordField();
        accessCodeField.setPromptText("Access Code");
        accessCodeField.getStyleClass().add("input-field");

        // Error message label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

        // Submit button
        Button submitButton = new Button("Submit");
        submitButton.setDefaultButton(true); // Triggers on Enter key
        submitButton.getStyleClass().add("submit-button");

        // Exit button
        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("exit-button");

        // --- Set Button Actions ---

        // Submit logic for checking access code
        submitButton.setOnAction(e -> {
            String code = accessCodeField.getText();
            if (code.equals("12345")) {
                errorLabel.setText("");
                EmployeeAccess.show();
            } else if (code.equals("admin123")) {
                errorLabel.setText("");
                AdminAccess.show();
            } else {
                errorLabel.setText("Invalid Access Code");
            }
        });

        // Exit app on exit button press
        exitButton.setOnAction(e -> Platform.exit());

        // --- Layout Setup ---

        VBox layout = new VBox(10, label, accessCodeField, submitButton, exitButton, errorLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));

        // Allow input and buttons to expand vertically
        VBox.setVgrow(accessCodeField, Priority.ALWAYS);
        VBox.setVgrow(submitButton, Priority.ALWAYS);
        VBox.setVgrow(exitButton, Priority.ALWAYS);

        // Set layout as the current view inside the root container
        AccessPage.root.getChildren().setAll(layout);
    }
}
