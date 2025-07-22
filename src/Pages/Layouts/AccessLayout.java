package Pages.Layouts;

import Pages.AccessPage;
import Pages.AdminAccess;
import Pages.EmployeeAccess;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AccessLayout {

    public static void show() {
        Label label = new Label("Enter Access Code:");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        PasswordField accessCodeField = new PasswordField();
        accessCodeField.setPromptText("Access Code");
        accessCodeField.setId("access-input");
        accessCodeField.setPrefWidth(250);
        accessCodeField.setPrefHeight(40); // reduced height

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button submitButton = new Button("🔓 Submit");
        submitButton.setDefaultButton(true);
        submitButton.getStyleClass().addAll("button", "primary-button");
        submitButton.setPrefWidth(250);

        Button exitButton = new Button("❌ Exit");
        exitButton.getStyleClass().addAll("button", "danger-button");
        exitButton.setPrefWidth(250);

        submitButton.setOnAction(e -> {
            String code = accessCodeField.getText();
            Stage currentStage = (Stage) AccessPage.root.getScene().getWindow();

            if (code.equals("12345")) {
                errorLabel.setText("");
                EmployeeAccess.show(currentStage);
            } else if (code.equals("admin123")) {
                errorLabel.setText("");
                AdminAccess.show(currentStage);
            } else {
                errorLabel.setText("Invalid Access Code");
            }
        });

        exitButton.setOnAction(e -> Platform.exit());

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setId("access-container");

        layout.getChildren().addAll(label, accessCodeField, submitButton, exitButton, errorLabel);

        AccessPage.root.getChildren().setAll(layout);
    }
}
