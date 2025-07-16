package Pages;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class AdminAccess {

    // ===== Left Navigation Bar =====
    public static void show() {
        VBox navBar = new VBox(20);
        navBar.getStyleClass().add("navbar");
        navBar.setAlignment(Pos.TOP_CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(navBar, Priority.ALWAYS);

        // ===== Title as two stacked labels =====
        Label title1 = new Label("Inventory");
        Label title2 = new Label("Manager");
        title1.getStyleClass().add("navbar-title-line");
        title2.getStyleClass().add("navbar-title-line");
        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(0);

        Button dashboardBtn = makeNavButton("Dashboard", "📊");
        Button inventoryBtn = makeNavButton("Inventory", "📦");
        Button reportsBtn = makeNavButton("Reports", "📈");
        Button logoutBtn = makeNavButton("Logout", "🔒");
        Button exitBtn = makeNavButton("Exit", "🚪");

        VBox topButtonBox = new VBox(15, wrap(dashboardBtn), wrap(inventoryBtn), wrap(reportsBtn));
        topButtonBox.setAlignment(Pos.TOP_CENTER);

        VBox bottomButtonBox = new VBox(15, wrap(logoutBtn), wrap(exitBtn));
        bottomButtonBox.setAlignment(Pos.BOTTOM_CENTER);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        navBar.getChildren().addAll(titleBox, topButtonBox, spacer, bottomButtonBox);

        BorderPane layout = new BorderPane();
        layout.setLeft(navBar);
        layout.setCenter(Pages.Layouts.AdminDashboardLayout.build());
        layout.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(navBar, Pos.TOP_LEFT);

        layout.widthProperty().addListener((obs, oldVal, newVal) -> {
            navBar.setPrefWidth(newVal.doubleValue() * 0.16);
        });

        StackPane.setAlignment(layout, Pos.CENTER);
        StackPane.setMargin(layout, Insets.EMPTY);
        AccessPage.root.getChildren().setAll(layout);

        // ===== Navigation Handlers =====
        dashboardBtn.setOnAction(e -> layout.setCenter(Pages.Layouts.AdminDashboardLayout.build()));
        inventoryBtn.setOnAction(e -> layout.setCenter(buildInventory()));
        reportsBtn.setOnAction(e -> layout.setCenter(buildReports()));
        logoutBtn.setOnAction(e -> AccessLayout.show());
        exitBtn.setOnAction(e -> Platform.exit());
    }

    // ===== Button Factory =====
    private static Button makeNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280);
        return btn;
    }

    // ===== Button Wrapper =====
    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    // ===== Placeholder Inventory Page =====
    private static VBox buildInventory() {
        Label title = new Label("Inventory Management (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dashboard");
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    // ===== Placeholder Reports Page =====
    private static VBox buildReports() {
        Label title = new Label("Reports Section (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dashboard");
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }
}
