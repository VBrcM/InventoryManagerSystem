package Pages;

import Pages.Layouts.AccessLayout;
import Pages.Layouts.Admin.AdminDashboardLayout;
import Pages.Layouts.Admin.AdminInventoryLayout;
import Pages.Layouts.Admin.AdminReportsLayout;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminAccess {

    public static void show(Stage stage) {
        // === Navigation Bar (Left Panel) ===
        VBox navBar = new VBox(20);
        navBar.getStyleClass().add("navbar");
        navBar.setAlignment(Pos.TOP_CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setPrefWidth(320); // Fixed width to match EmployeeAccess
        navBar.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(navBar, Priority.ALWAYS);

        // === Title Section ===
        Label title1 = new Label("Admin");
        Label title2 = new Label("Portal");
        title1.getStyleClass().add("navbar-title-line");
        title2.getStyleClass().add("navbar-title-line");

        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(4);

        // === Navigation Buttons ===
        Button dashboardBtn = makeNavButton("Dashboard", "📊");
        Button inventoryBtn = makeNavButton("Inventory", "📦");
        Button reportsBtn = makeNavButton("Sales Reports", "📈");
        Button logoutBtn = makeNavButton("Logout", "🔒");
        Button exitBtn = makeNavButton("Exit", "🚪");

        // === Button Layouts ===
        // Top Section Buttons
        VBox topButtons = new VBox(15,
                wrap(dashboardBtn),
                wrap(inventoryBtn)
        );
        topButtons.setAlignment(Pos.TOP_CENTER);

        // Right-aligned Reports Button
        HBox reportsRow = new HBox(wrap(reportsBtn));
        reportsRow.setAlignment(Pos.CENTER_RIGHT);

        VBox topButtonBox = new VBox(15, topButtons, reportsRow);
        topButtonBox.setAlignment(Pos.TOP_CENTER);

        // Bottom Section Buttons (Logout, Exit)
        VBox bottomButtonBox = new VBox(15,
                wrap(logoutBtn),
                wrap(exitBtn)
        );
        bottomButtonBox.setAlignment(Pos.BOTTOM_CENTER);

        // Spacer to push bottom buttons downward
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // === Assemble Navbar ===
        navBar.getChildren().addAll(
                titleBox,
                topButtonBox,
                spacer,
                bottomButtonBox
        );

        // === Main Application Layout ===
        BorderPane layout = new BorderPane();
        layout.setLeft(navBar);
        layout.setCenter(AdminDashboardLayout.build(layout)); // Default view
        layout.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(navBar, Pos.TOP_LEFT);

        // === Add to Root StackPane ===
        StackPane.setAlignment(layout, Pos.CENTER);
        StackPane.setMargin(layout, Insets.EMPTY);
        AccessPage.root.getChildren().setAll(layout);

        // === Button Event Handlers ===
        dashboardBtn.setOnAction(e -> layout.setCenter(AdminDashboardLayout.build(layout)));
        inventoryBtn.setOnAction(e -> layout.setCenter(AdminInventoryLayout.build()));
        reportsBtn.setOnAction(e -> layout.setCenter(AdminReportsLayout.build(layout)));
        logoutBtn.setOnAction(e -> AccessLayout.show());
        exitBtn.setOnAction(e -> Platform.exit());
    }

    /**
     * Creates a styled navigation button with icon and text.
     *
     * @param text Display text for the button
     * @param icon Emoji or symbolic icon prefix
     * @return Configured Button instance
     */
    private static Button makeNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280); // Consistent with EmployeeAccess layout
        return btn;
    }

    /**
     * Wraps a button in a centered HBox for layout alignment.
     *
     * @param button Button to wrap
     * @return HBox containing the button
     */
    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }
}
