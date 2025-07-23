package Pages;

import Pages.Layouts.AccessLayout;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminAccess {

    public static void show(Stage stage) {
        // Navigation Bar
        VBox navBar = new VBox(20);
        navBar.getStyleClass().add("navbar");
        navBar.setAlignment(Pos.TOP_CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(navBar, Priority.ALWAYS);

        // Title
        Label title1 = new Label("Inventory");
        Label title2 = new Label("Manager");
        title1.getStyleClass().add("navbar-title-line");
        title2.getStyleClass().add("navbar-title-line");
        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(4);

        // Navigation Buttons
        Button dashboardBtn = makeNavButton("Dashboard", "📊");
        Button inventoryBtn = makeNavButton("Inventory", "📦");
        Button reportsBtn = makeNavButton("Sales Reports", "📈");
        Button logoutBtn = makeNavButton("Logout", "🔒");
        Button exitBtn = makeNavButton("Exit", "🚪");

        VBox topButtons = new VBox(15, wrap(dashboardBtn), wrap(inventoryBtn));
        topButtons.setAlignment(Pos.TOP_CENTER);

        HBox reportsRow = new HBox(wrap(reportsBtn));
        reportsRow.setAlignment(Pos.CENTER_RIGHT);

        VBox topButtonBox = new VBox(15, topButtons, reportsRow);
        topButtonBox.setAlignment(Pos.TOP_CENTER);

        VBox bottomButtonBox = new VBox(15, wrap(logoutBtn), wrap(exitBtn));
        bottomButtonBox.setAlignment(Pos.BOTTOM_CENTER);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        navBar.getChildren().addAll(titleBox, topButtonBox, spacer, bottomButtonBox);

        // Main Layout
        BorderPane layout = new BorderPane();
        layout.setLeft(navBar);
        layout.setCenter(Pages.Layouts.AdminDashboardLayout.build(layout));
        layout.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        layout.widthProperty().addListener((obs, oldVal, newVal) -> {
            navBar.setPrefWidth(newVal.doubleValue() * 0.16);
        });

        StackPane.setAlignment(layout, Pos.CENTER);
        AccessPage.root.getChildren().setAll(layout);

        // Button Actions
        dashboardBtn.setOnAction(e -> layout.setCenter(Pages.Layouts.AdminDashboardLayout.build(layout)));
        inventoryBtn.setOnAction(e -> layout.setCenter(Pages.Layouts.AdminInventoryLayout.build()));
        reportsBtn.setOnAction(e -> layout.setCenter(Pages.Layouts.AdminReportsLayout.build(layout)));
        logoutBtn.setOnAction(e -> AccessLayout.show());
        exitBtn.setOnAction(e -> Platform.exit());
    }

    private static Button makeNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280);
        return btn;
    }

    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }
}