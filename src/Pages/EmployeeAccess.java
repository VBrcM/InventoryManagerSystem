package Pages;

import Pages.Layouts.AccessLayout;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EmployeeAccess {

    public static void show(Stage stage) {
        // ===== Navigation Bar (Left Side) =====
        VBox navBar = new VBox(20);
        navBar.getStyleClass().add("navbar");
        navBar.setAlignment(Pos.TOP_CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(navBar, Priority.ALWAYS);

        // ===== Title (Inventory Manager) =====
        Label title1 = new Label("Inventory");
        Label title2 = new Label("Manager");
        title1.getStyleClass().add("navbar-title-line");
        title2.getStyleClass().add("navbar-title-line");
        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(0);

        // ===== Navigation Buttons =====
        Button dashboardBtn = makeNavButton("Dashboard", "📊");
        Button transactionBtn = makeNavButton("Transaction", "📦");
        Button transRecordsBtn = makeNavButton("Transaction Records", "📈");
        Button logoutBtn = makeNavButton("Logout", "🔒");
        Button exitBtn = makeNavButton("Exit", "🚪");

        // ===== Grouping Top and Bottom Buttons =====
        VBox topButtonBox = new VBox(15, wrap(dashboardBtn), wrap(transactionBtn), wrap(transRecordsBtn));
        topButtonBox.setAlignment(Pos.TOP_CENTER);

        VBox bottomButtonBox = new VBox(15, wrap(logoutBtn), wrap(exitBtn));
        bottomButtonBox.setAlignment(Pos.BOTTOM_CENTER);

        // ===== Spacer Between Top and Bottom Buttons =====
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ===== Add All Components to Navigation Bar =====
        navBar.getChildren().addAll(titleBox, topButtonBox, spacer, bottomButtonBox);

        // ===== Main Layout =====
        BorderPane layout = new BorderPane();
        layout.setLeft(navBar);
        layout.setCenter(buildReports()); // Default view
        layout.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(navBar, Pos.TOP_LEFT);

        // ===== Responsive Navbar Width (16% of screen width) =====
        layout.widthProperty().addListener((obs, oldVal, newVal) -> {
            navBar.setPrefWidth(newVal.doubleValue() * 0.16);
        });

        // ===== Set Layout in Root StackPane =====
        StackPane.setAlignment(layout, Pos.CENTER);
        StackPane.setMargin(layout, Insets.EMPTY);
        AccessPage.root.getChildren().setAll(layout);

        // ===== Button Actions =====
        dashboardBtn.setOnAction(e -> layout.setCenter(buildReports()));
        transactionBtn.setOnAction(e -> layout.setCenter(buildReports()));
        transRecordsBtn.setOnAction(e -> layout.setCenter(buildReports()));
        logoutBtn.setOnAction(e -> AccessLayout.show());
        exitBtn.setOnAction(e -> Platform.exit());
    }

    // ===== Helper to Create a Styled Navigation Button =====
    private static Button makeNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280);
        return btn;
    }

    // ===== Wrap Button in Centered HBox for Alignment =====
    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    // ===== Placeholder Inventory Page (Unused) =====
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
        Label title = new Label("Section (Placeholder)");
        title.setId("title-label");

        VBox box = new VBox(title);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dashboard");
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }
}
