package Pages.Layouts;

import Model.POJO.SaleItem;
import Model.DAO.SaleItemDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminReportsLayout {

    private static int maxMonths = 1;
    private static int totalDaysLoaded = 0;
    private static final int DAYS_PER_SHOW_MORE = 10;

    // Builds the main layout for displaying sales reports
    public static VBox build(BorderPane parentLayout) {
        totalDaysLoaded = 0;

        // Title label for reports page
        Label title = new Label("Sales Reports");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));

        // Dropdown for filter by time range
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Last 1 month", "Last 2 months", "Last 3 months");
        filterBox.setValue("Last 1 month");
        filterBox.getStyleClass().add("inventory-button");

        // VBox to store each day's report summary
        VBox dayList = new VBox(15);
        dayList.setPadding(new Insets(10));
        dayList.setAlignment(Pos.TOP_CENTER);

        // Footer box to hold the "Show More" or "No records" message
        VBox footerBox = new VBox();
        footerBox.setAlignment(Pos.CENTER);
        dayList.getChildren().add(footerBox);

        // Logic for when filter is changed
        filterBox.setOnAction(e -> {
            System.out.println("Filter changed to: " + filterBox.getValue());
            totalDaysLoaded = 0;
            maxMonths = switch (filterBox.getValue()) {
                case "Last 1 month" -> 1;
                case "Last 2 months" -> 2;
                case "Last 3 months" -> 3;
                default -> 1;
            };
            dayList.getChildren().removeIf(node -> node != footerBox);
            footerBox.getChildren().clear();
            loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);
        });

        // Layout for filter dropdown
        HBox filterBar = new HBox(filterBox);
        filterBar.setAlignment(Pos.CENTER_RIGHT);
        filterBar.setPadding(new Insets(0, 0, 10, 0));

        // Scroll container to allow scrolling through the day summaries
        ScrollPane scrollPane = new ScrollPane(dayList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Final root layout
        VBox root = new VBox(10, title, filterBar, scrollPane);
        root.setPadding(new Insets(30));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);

        return root;
    }

    // Creates a visual summary card for a specific date with total sales
    private static VBox createDaySummary(LocalDate date, DateTimeFormatter formatter, List<SaleItem> transactions, BorderPane layout) {
        Label dateLabel = new Label("📅 " + date.format(formatter));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        double total = transactions.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
        Label totalLabel = new Label("Total Sales: ₱" + total);
        totalLabel.setStyle("-fx-text-fill: #cccccc;");

        List<SaleItem> saleItems = SaleItemDAO.getSaleItemsByDate(date);

        // Button to view detailed sales report of the day
        Button viewBtn = new Button("View Details");
        viewBtn.setOnAction(e -> {
            System.out.println("Viewing details for: " + date);
            layout.setCenter(AdminReportDetailsLayout.build(layout, date, saleItems));
        });
        viewBtn.getStyleClass().add("inventory-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, dateLabel, spacer, viewBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        VBox card = new VBox(10, header, totalLabel);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        return card;
    }

    // Loads a batch of days (summaries) into the view
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

            List<SaleItem> transactions = SaleItemDAO.getSaleItemsByDate(date);
            if (!transactions.isEmpty()) {
                System.out.println("Adding report for date: " + date);
                VBox dayCard = createDaySummary(date, formatter, transactions, layout);
                dayList.getChildren().add(dayList.getChildren().size() - 1, dayCard);
                added++;
                totalDaysLoaded++;
            }
        }

        boolean moreAvailable = totalDaysLoaded < allDates.size();

        // Show "Show More" button if more records are available
        if (moreAvailable) {
            Button more = new Button("Show More");
            more.getStyleClass().add("inventory-button");
            more.setOnAction(ev -> {
                System.out.println("Loading more records...");
                loadMoreDays(dayList, layout, footerBox, DAYS_PER_SHOW_MORE, false);
            });
            footerBox.getChildren().add(more);
        } else {
            Label done = new Label("No more sales records available");
            done.setStyle("-fx-text-fill: #888; -fx-padding: 10 0 20 0;");
            footerBox.getChildren().add(done);
        }
    }

    // Utility method to refresh/rebuild the layout
    public static void refresh(BorderPane parentLayout) {
        System.out.println("Refreshing reports layout...");
        parentLayout.setCenter(build(parentLayout));
    }
}
