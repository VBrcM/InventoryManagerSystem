package Pages;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class AdminAccess {

    public static void show() {
        // --- Left Navigation Bar ---
        VBox navBar = new VBox(20);
        navBar.setPadding(new Insets(40));
        navBar.setStyle("-fx-background-color: #2a2a2a;");
        navBar.setPrefWidth(250);
        navBar.setAlignment(Pos.TOP_LEFT);

        // Navigation buttons
        Button dashboardBtn = new Button("Dashboard");
        Button inventoryBtn = new Button("Inventory");
        Button reportsBtn = new Button("Reports");
        Button logoutBtn = new Button("Logout");
        Button exitBtn = new Button("Exit");

        // Apply shared button style
        dashboardBtn.getStyleClass().add("nav-button");
        inventoryBtn.getStyleClass().add("nav-button");
        reportsBtn.getStyleClass().add("nav-button");
        logoutBtn.getStyleClass().add("nav-button");
        exitBtn.getStyleClass().add("nav-button");

        // Add buttons to nav bar
        navBar.getChildren().addAll(dashboardBtn, inventoryBtn, reportsBtn, logoutBtn, exitBtn);

        // --- Root Layout Setup ---
        final BorderPane layout = new BorderPane();
        layout.setLeft(navBar);  // Add nav bar to the left side
        layout.setCenter(Pages.Layouts.AdminDashboardLayout.build()); // Default view
        layout.setStyle("-fx-background-color: #1e1e1e;");

        // Display layout in root StackPane
        AccessPage.root.getChildren().setAll(layout);

        // --- Navigation Handlers ---
        dashboardBtn.setOnAction(e -> layout.setCenter(buildDashboard()));
        inventoryBtn.setOnAction(e -> layout.setCenter(buildInventory()));
        reportsBtn.setOnAction(e -> layout.setCenter(buildReports()));
        logoutBtn.setOnAction(e -> AccessLayout.show());
        exitBtn.setOnAction(e -> Platform.exit());
    }

    // --- Dashboard Layout Content ---
    private static VBox buildDashboard() {
        Label title = new Label("Admin Dashboard");
        title.setId("title-label");

        Label totalItemsLabel = new Label("Total Items: ");
        Label totalEmployeesLabel = new Label("Total Employees: ");
        Label lowStockLabel = new Label("Low Stock Alerts: ");
        Label recentActivityLabel = new Label("Recent Activity: ");

        // Set IDs for styling or dynamic updates
        totalItemsLabel.setId("total-items");
        totalEmployeesLabel.setId("total-employees");
        lowStockLabel.setId("low-stock");
        recentActivityLabel.setId("recent-activity");

        VBox box = new VBox(20, title, totalItemsLabel, totalEmployeesLabel, lowStockLabel, recentActivityLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(40));
        box.getStyleClass().add("dashboard");

        return box;
    }

    // --- Inventory Placeholder ---
    private static VBox buildInventory() {
        Label title = new Label("Inventory Management (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #1e1e1e;");
        return box;
    }

    // --- Reports Placeholder ---
    private static VBox buildReports() {
        Label title = new Label("Reports Section (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setStyle("-fx-background-color: #1e1e1e;");
        return box;
    }
}
