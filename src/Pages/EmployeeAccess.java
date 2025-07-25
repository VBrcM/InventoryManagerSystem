package Pages;

import Pages.Layouts.*;
import Pages.Layouts.Employee.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class EmployeeAccess {
    public static BorderPane layout;
    private static final Logger logger = Logger.getLogger(EmployeeAccess.class.getName());

    /**
     * Returns the main layout pane for the employee view.
     */
    public static BorderPane getLayout() {
        return layout;
    }

    /**
     * Initializes and displays the employee access screen using the provided stage.
     */
    public static void show(Stage stage) {
        layout = new BorderPane();

        // Setup the navigation bar and default center view
        setupNavBar();
        showDashboard();

        // Apply layout to root
        StackPane root = (StackPane) stage.getScene().getRoot();
        root.getChildren().setAll(layout);

        logger.info("Employee access screen loaded.");
    }

    /**
     * Sets up the left-hand navigation bar for employee actions.
     */
    private static void setupNavBar() {
        VBox navBar = new VBox(20);
        navBar.getStyleClass().add("navbar");
        navBar.setAlignment(Pos.TOP_CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setPrefWidth(320); // Fixed width consistent with AdminAccess
        navBar.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(navBar, Priority.ALWAYS);

        // Title section ("Employee Portal")
        Label title1 = new Label("AtariSync");
        Label title2 = new Label("Employee Portal");
        title1.getStyleClass().add("company-name");
        title1.setStyle("-fx-font-size: 40px;");
        title2.getStyleClass().add("navbar-title-line");

        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(4);

        // Navigation buttons
        Button dashboardBtn = createNavButton("Dashboard", "📊", EmployeeAccess::showDashboard);
        Button salesBtn = createNavButton("New Sale", "💰", EmployeeAccess::showSales);
        Button inventoryBtn = createNavButton("Inventory", "📦", EmployeeAccess::showInventory);
        Button todaysSalesBtn = createNavButton("Today's Sales", "📈", EmployeeAccess::showTodaysSales);
        Button salesLogBtn = createNavButton("Sales Log", "📈", EmployeeAccess::showSalesLogs);
        Button logoutBtn = createNavButton("Logout", "🔒", AccessLayout::show);
        Button exitBtn = createNavButton("Exit", "🚪", () -> {
            logger.info("Application exiting from EmployeeAccess.");
            System.exit(0);
        });

        // Grouping navigation buttons
        VBox topButtons = new VBox(15,
                wrap(dashboardBtn),
                wrap(salesBtn),
                wrap(inventoryBtn),
                wrap(todaysSalesBtn),
                wrap(salesLogBtn)
        );

        VBox bottomButtons = new VBox(15,
                wrap(logoutBtn),
                wrap(exitBtn)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Assemble navbar
        navBar.getChildren().addAll(titleBox, topButtons, spacer, bottomButtons);
        layout.setLeft(navBar);

        logger.info("Navigation bar setup completed.");
    }

    // Sets the center view to the dashboard
    private static void showDashboard() {
        layout.setCenter(EmployeeDashboardLayout.build());
        logger.fine("Dashboard view displayed.");
    }

    // Sets the center view to the sales page
    private static void showSales() {
        layout.setCenter(EmployeeSalesLayout.build(layout));
        logger.fine("Sales view displayed.");
    }

    // Sets the center view to the inventory
    private static void showInventory() {
        layout.setCenter(EmployeeInventoryLayout.build());
        logger.fine("Inventory view displayed.");
    }

    // Sets the center view to today's transactions
    private static void showTodaysSales() {
        layout.setCenter(EmployeeTransactionLayout.build());
        logger.fine("Today's transactions view displayed.");
    }

    // Sets the center view to transaction log
    private static void showSalesLogs() {
        layout.setCenter(EmployeeTransactionLogLayout.build(layout));
        logger.fine("Transaction log view displayed.");
    }

    /**
     * Creates a styled navigation button with an icon and label, and binds it to an action.
     */
    private static Button createNavButton(String text, String icon, Runnable action) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280); // Consistent width
        btn.setOnAction(e -> {
            logger.fine("Navigation button clicked: " + text);
            action.run();
        });
        return btn;
    }

    /**
     * Wraps a button in a centered HBox for consistent alignment in VBox.
     */
    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    /**
     * Navigates to a specific view based on the provided identifier.
     */
    public static void navigateTo(String view) {
        switch (view.toLowerCase()) {
            case "sales": showSales(); break;
            case "inventory": showInventory(); break;
            case "todays sales": showTodaysSales(); break;
            case "sales log": showSalesLogs(); break;
            default: showDashboard();
        }
        logger.fine("Navigated to view: " + view);
    }
}