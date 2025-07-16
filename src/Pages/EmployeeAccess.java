package Pages;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class EmployeeAccess {

    public static void show() {
        // --- Navigation Bar ---
        VBox navBar = new VBox(20);
        navBar.setPadding(new Insets(40));
        navBar.setStyle("-fx-background-color: #2a2a2a;");
        navBar.setPrefWidth(250);
        navBar.setAlignment(Pos.TOP_LEFT);

        // --- Navigation Buttons ---
        Button dashboardBtn = new Button("Dashboard");
        Button salesBtn = new Button("Sales Panel");
        Button logsBtn = new Button("Activity Logs");
        Button logoutBtn = new Button("Logout");
        Button exitBtn = new Button("Exit");

        // Apply CSS styling to buttons
        dashboardBtn.getStyleClass().add("nav-button");
        salesBtn.getStyleClass().add("nav-button");
        logsBtn.getStyleClass().add("nav-button");
        logoutBtn.getStyleClass().add("nav-button");
        exitBtn.getStyleClass().add("nav-button");

        // --- Button Actions ---
        logoutBtn.setOnAction(e -> AccessLayout.show());
        exitBtn.setOnAction(e -> Platform.exit());

        navBar.getChildren().addAll(dashboardBtn, salesBtn, logsBtn, logoutBtn, exitBtn);

        // --- Main Layout ---
        final BorderPane layout = new BorderPane();
        layout.setLeft(navBar);
        layout.setCenter(buildDashboard()); // Set default view
        layout.setStyle("-fx-background-color: #1e1e1e;");
        AccessPage.root.getChildren().setAll(layout);

        // --- Navigation Handlers ---
        dashboardBtn.setOnAction(e -> layout.setCenter(buildDashboard()));
        salesBtn.setOnAction(e -> layout.setCenter(buildSalesPanel()));
        logsBtn.setOnAction(e -> layout.setCenter(buildLogs()));
    }

    // --- Dashboard Placeholder ---
    private static VBox buildDashboard() {
        Label title = new Label("Employee Dashboard");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #1e1e1e;");
        return box;
    }

    // --- Sales Panel Placeholder ---
    private static VBox buildSalesPanel() {
        Label title = new Label("Sales Panel (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #1e1e1e;");
        return box;
    }

    // --- Activity Logs Placeholder ---
    private static VBox buildLogs() {
        Label title = new Label("Activity Logs (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #1e1e1e;");
        return box;
    }
}
