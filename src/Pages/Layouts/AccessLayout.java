package Pages.Layouts;

import Pages.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.logging.Logger;

public class AccessLayout {

    private static final Logger logger = Logger.getLogger(AccessLayout.class.getName());

    /**
     * Displays the access code login layout and handles access logic.
     */
    public static void show() {
        Label companyName = new Label("AtariSync");
        companyName.setStyle("-fx-font-size: 70px;");
        companyName.getStyleClass().addAll("company-name");
        VBox.setMargin(companyName, new Insets(0, 0, 40, 0));

        // Label for prompt
        Label label = new Label("Enter Access Code:");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        // Access code input field
        PasswordField accessCodeField = new PasswordField();
        accessCodeField.setPromptText("Access Code");
        accessCodeField.setId("access-input");
        accessCodeField.setPrefWidth(250);
        accessCodeField.setPrefHeight(40);

        // Label to show errors
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        // Submit button
        Button submitButton = new Button("🔓 Submit");
        submitButton.setDefaultButton(true);
        submitButton.getStyleClass().addAll("button", "primary-button");
        submitButton.setPrefWidth(250);

        // Exit button
        Button exitButton = new Button("❌ Exit");
        exitButton.getStyleClass().addAll("button", "danger-button");
        exitButton.setPrefWidth(250);

        // Access verification
        submitButton.setOnAction(e -> {
            String code = accessCodeField.getText();
            Stage currentStage = (Stage) AccessPage.root.getScene().getWindow();

            logger.info("Access attempt with code: " + code);

            if (code.equals("12345")) {
                logger.info("Employee access granted");
                errorLabel.setText("");
                EmployeeAccess.show(currentStage);
            } else if (code.equals("admin123")) {
                logger.info("Admin access granted");
                errorLabel.setText("");
                AdminAccess.show(currentStage);
            } else {
                logger.warning("Invalid access code entered");
                errorLabel.setText("Invalid Access Code");
            }
        });

        // Exit application
        exitButton.setOnAction(e -> {
            logger.info("Application exit triggered");
            Platform.exit();
        });

        // Layout setup
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setId("access-container");

        layout.getChildren().addAll(companyName, label, accessCodeField, submitButton, exitButton, errorLabel);

        // Display on root pane
        AccessPage.root.getChildren().setAll(layout);
    }
}
