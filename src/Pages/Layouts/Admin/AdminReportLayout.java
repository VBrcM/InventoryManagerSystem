package Pages.Layouts.Admin;

import DB.*;
import Model.POJO.*;
import Model.DAO.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds and displays the admin sales report layout showing daily summaries
 * and providing access to detailed transaction views.
 */
public class AdminReportLayout {

    private static final Logger LOGGER = Logger.getLogger(AdminReportLayout.class.getName());

    private static int maxMonths = 1;
    private static int totalDaysLoaded = 0;
    private static final int DAYS_PER_SHOW_MORE = 10;

    /**
     * Returns the full scrollable VBox layout for the report.
     */
    public static VBox build(BorderPane parentLayout) {
        totalDaysLoaded = 0;

        // Title
        Label title = new Label("Sales Reports");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 10, 0));

        // Filter
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Last 1 month", "Last 2 months", "Last 3 months");
        filterBox.setValue("Last 1 month");
        filterBox.getStyleClass().add("inventory-button");
        filterBox.setPrefSize(140, 32);

        // Title + Filter Layout
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleSection = new HBox(10, title, spacer, filterBox);
        titleSection.setAlignment(Pos.CENTER_LEFT);
        titleSection.setPadding(new Insets(0, 0, 5, 0));

        // Day Summary Container
        VBox dayList = new VBox(15);
        dayList.setPadding(new Insets(10));
        dayList.setAlignment(Pos.TOP_CENTER);

        // Footer box for "Show More"
        VBox footerBox = new VBox();
        footerBox.setAlignment(Pos.CENTER);
        dayList.getChildren().add(footerBox);

        // Filter Change
        filterBox.setOnAction(e -> {
            totalDaysLoaded = 0;
            maxMonths = switch (filterBox.getValue()) {
                case "Last 2 months" -> 2;
                case "Last 3 months" -> 3;
                default -> 1;
            };
            dayList.getChildren().removeIf(node -> node != footerBox);
            footerBox.getChildren().clear();
            loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);
        });

        // Scroll Container
        ScrollPane scrollPane = new ScrollPane(dayList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Root Container
        VBox root = new VBox(10, titleSection, scrollPane);
        root.getStyleClass().add("root-panel");
        root.setPadding(new Insets(30));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Initial Load
        loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);

        return root;
    }

    /**
     * Loads a batch of daily summaries based on the selected filter.
     */
    private static void loadMoreDays(VBox dayList, BorderPane layout, VBox footerBox, int daysToLoad, boolean respectFilterLimit) {
        List<LocalDate> allDates = SaleItemDAO.getAllTransactionDates();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusMonths(maxMonths);

        int added = 0;
        footerBox.getChildren().clear();

        for (int i = totalDaysLoaded; i < allDates.size() && added < daysToLoad; i++) {
            LocalDate date = allDates.get(i);
            if (respectFilterLimit && date.isBefore(minDate)) break;

            List<SaleItem> transactions;
            try {
                transactions = SaleItemDAO.getSaleItemsByDate(date);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to fetch sales for " + date, e);
                continue;
            }

            if (!transactions.isEmpty()) {
                VBox dayCard = createDaySummary(date, formatter, transactions, layout);
                dayList.getChildren().add(dayList.getChildren().size() - 1, dayCard);
                added++;
                totalDaysLoaded++;
            }
        }

        boolean moreAvailable = totalDaysLoaded < allDates.size();
        if (moreAvailable) {
            // Show More button
            Button more = new Button("Show More");
            more.getStyleClass().add("inventory-button");
            more.setOnAction(ev -> loadMoreDays(dayList, layout, footerBox, DAYS_PER_SHOW_MORE, false));
            footerBox.getChildren().add(more);
        } else {
            // No more records message
            Label done = new Label("No more sales records available");
            done.setStyle("-fx-text-fill: #888; -fx-padding: 10 0 20 0;");
            footerBox.getChildren().add(done);
        }
    }

    /**
     * Creates a summary card with date, total sales, and view button.
     */
    private static VBox createDaySummary(LocalDate date, DateTimeFormatter formatter, List<SaleItem> transactions, BorderPane layout) {
        Label dateLabel = new Label("📅 " + date.format(formatter));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        double total = transactions.stream()
                .mapToDouble(item -> item.getSiQty() * item.getSiPrice())
                .sum();

        Label totalLabel = new Label("Total Sales: " + AppFormatter.formatCurrency(total));
        totalLabel.setStyle("-fx-text-fill: #cccccc;");

        // View Details button
        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("inventory-button");
        viewBtn.setOnAction(e -> layout.setCenter(AdminReportDetailsLayout.build(layout, date)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, dateLabel, spacer, viewBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, header, totalLabel);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");

        return card;
    }

    /**
     * Refreshes the layout by rebuilding with the latest data.
     */
    public static void refresh(BorderPane parentLayout) {
        parentLayout.setCenter(build(parentLayout));
    }
}