package Pages;


import Pages.Layouts.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EmployeeAccess {
    private static BorderPane layout;
    private static Stage currentStage;

    public static void show(Stage stage) {
        currentStage = stage;
        layout = new BorderPane();

        // Setup navigation and initial view
        setupNavBar();
        showDashboard();

        // Apply to stage
        StackPane root = (StackPane) stage.getScene().getRoot();
        root.getChildren().setAll(layout);
    }

    private static void setupNavBar() {
        VBox navBar = new VBox(20);
        navBar.getStyleClass().add("navbar");
        navBar.setAlignment(Pos.TOP_CENTER);
        navBar.setPadding(new Insets(10, 20, 10, 20));

        // Title
        Label title1 = new Label("Employee");
        Label title2 = new Label("Portal");
        title1.getStyleClass().add("navbar-title-line");
        title2.getStyleClass().add("navbar-title-line");
        VBox titleBox = new VBox(title1, title2);
        titleBox.setAlignment(Pos.CENTER);

        // Navigation Buttons
        Button dashboardBtn = createNavButton("Dashboard", "📊", () -> showDashboard());
        Button salesBtn = createNavButton("New Sale", "💰", () -> showSales());
        Button inventoryBtn = createNavButton("Inventory", "📦", () -> showInventory());
        Button transactionsBtn = createNavButton("Today's Transactions", "📈", () -> showTransactions());
        Button transactionsLogBtn = createNavButton("Transaction Log", "📈", () -> showTransactions());
        Button logoutBtn = createNavButton("Logout", "🔒", () -> AccessLayout.show());
        Button exitBtn = createNavButton("Exit", "🚪", () -> System.exit(0));

        // Layout
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

        navBar.getChildren().addAll(titleBox, topButtons, spacer, bottomButtons);
        layout.setLeft(navBar);
    }

    // View Controllers
    private static void showDashboard() {
        VBox dashboard = EmployeeDashboardLayout.build();
        layout.setCenter(dashboard);
        loadDashboardData(dashboard);
    }

    private static void showSales() {
        layout.setCenter(EmployeeSalesLayout.build(layout));
    }

    private static void showInventory() {
        layout.setCenter(EmployeeInventoryLayout.build());
    }

    private static void showTransactions() {
        layout.setCenter(EmployeeTransactionsLayout.build());
    }
    private static void showTransactionsLogs() {
        layout.setCenter(EmployeeTransactionLogLayout.build(layout));
    }

    // Data Loading
    private static void loadDashboardData(VBox dashboard) {
        try {
            HBox statsRow = (HBox) dashboard.getChildren().get(2);
            VBox salesBox = (VBox) statsRow.getChildren().get(0);
            VBox transactionsBox = (VBox) statsRow.getChildren().get(1);
            VBox popularItemBox = (VBox) statsRow.getChildren().get(2);
            VBox lowStockBox = (VBox) statsRow.getChildren().get(3);
            VBox transactionsList = (VBox) dashboard.lookup("#transactionsList");

            // Mock data - replace with database calls
            ((Label) salesBox.getChildren().get(0)).setText("₱1,245.50");
            ((Label) transactionsBox.getChildren().get(0)).setText("18");
            ((Label) popularItemBox.getChildren().get(0)).setText("Blue Pens");
            ((Label) lowStockBox.getChildren().get(0)).setText("2 items");

            if (transactionsList != null) {
                transactionsList.getChildren().clear();
                addTransactionItem(transactionsList, "09:30 AM", "₱250.00", "Notebook (2), Pens (3)");
                addTransactionItem(transactionsList, "10:45 AM", "₱180.50", "Binder (1), Paper (5)");
                addTransactionItem(transactionsList, "11:15 AM", "₱75.25", "Pencils (10), Erasers (2)");
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }

    // UI Components
    private static Button createNavButton(String text, String icon, Runnable action) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(50);
        btn.setPrefWidth(280);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private static HBox wrap(Button button) {
        HBox wrapper = new HBox(button);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

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

    // Helper for quick actions in dashboard
    public static void navigateTo(String view) {
        switch (view) {
            case "sales": showSales(); break;
            case "inventory": showInventory(); break;
            case "transactions": showTransactions(); break;
            case "transaction log": showTransactions(); break;
            default: showDashboard();
        }
    }
}
