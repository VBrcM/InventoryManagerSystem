package Pages;

import Pages.Layouts.*;
import Pages.Layouts.Employee.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EmployeeAccess {
    public static BorderPane layout;
    private static Stage currentStage;

    /**
     * Returns the main layout pane for the employee view.
     */
    public static BorderPane getLayout() {
        return layout;
    }

    /**
     * Initializes and displays the employee access screen.
     *
     * @param stage The primary stage to display the layout on
     */
    public static void show(Stage stage) {
        currentStage = stage;
        layout = new BorderPane();

        // Setup the navigation bar and default center view
        setupNavBar();
        showDashboard();

        // Apply layout to root
        StackPane root = (StackPane) stage.getScene().getRoot();
        root.getChildren().setAll(layout);
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
        Label title1 = new Label("Employee");
        Label title2 = new Label("Portal");
        title1.getStyleClass().add("navbar-title-line");
        title2.getStyleClass().add("navbar-title-line");

        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(4);

        // Navigation buttons
        Button dashboardBtn = createNavButton("Dashboard", "📊", EmployeeAccess::showDashboard);
        Button salesBtn = createNavButton("New Sale", "💰", EmployeeAccess::showSales);
        Button inventoryBtn = createNavButton("Inventory", "📦", EmployeeAccess::showInventory);
        Button transactionsBtn = createNavButton("Today's Transactions", "📈", EmployeeAccess::showTransactions);
        Button transactionsLogBtn = createNavButton("Transaction Log", "📈", EmployeeAccess::showTransactionsLogs);
        Button logoutBtn = createNavButton("Logout", "🔒", AccessLayout::show);
        Button exitBtn = createNavButton("Exit", "🚪", () -> System.exit(0));

        // Grouping navigation buttons
        VBox topButtons = new VBox(15,
                wrap(dashboardBtn),
                wrap(salesBtn),
                wrap(inventoryBtn),
                wrap(transactionsBtn),
                wrap(transactionsLogBtn)
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
    }

    // === View Setters ===

    private static void showDashboard() {
        layout.setCenter(EmployeeDashboardLayout.build());
    }

    private static void showSales() {
        layout.setCenter(EmployeeSalesLayout.build(layout));
    }

    private static void showInventory() {
        layout.setCenter(EmployeeInventoryLayout.build());
    }

    private static void showTransactions() {
        layout.setCenter(EmployeeTransactionLayout.build());
    }

    private static void showTransactionsLogs() {
        layout.setCenter(EmployeeTransactionLogLayout.build(layout));
    }

    // === UI Helpers ===

    /**
     * Creates a styled navigation button with an icon and label.
     *
     * @param text   The button label
     * @param icon   A string-based icon (e.g., emoji)
     * @param action The action to execute on click
     * @return Configured Button instance
     */
    private static Button createNavButton(String text, String icon, Runnable action) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280); // Consistent width
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * Wraps a button in a centered HBox for alignment inside VBox.
     *
     * @param button The button to wrap
     * @return HBox containing the button
     */
    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    /**
     * Adds a styled transaction item row (used inside dashboard widgets).
     *
     * @param container The container to add the row to
     * @param time      Timestamp label
     * @param amount    Transaction amount
     * @param items     Description or item list
     */
    private static void addTransactionItem(VBox container, String time, String amount, String items) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 8;");

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 100;");

        Label amountLabel = new Label(amount);
        amountLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-min-width: 100;");

        Label itemsLabel = new Label(items);
        itemsLabel.setStyle("-fx-text-fill: #aaaaaa;");
        itemsLabel.setWrapText(true);
        HBox.setHgrow(itemsLabel, Priority.ALWAYS);

        item.getChildren().addAll(timeLabel, amountLabel, itemsLabel);
        container.getChildren().add(item);
    }

    /**
     * Allows external dashboard shortcuts to navigate to specific views.
     *
     * @param view String identifier for the view
     */
    public static void navigateTo(String view) {
        switch (view.toLowerCase()) {
            case "sales": showSales(); break;
            case "inventory": showInventory(); break;
            case "transactions": showTransactions(); break;
            case "transaction log": showTransactionsLogs(); break;
            default: showDashboard();
        }
    }
}