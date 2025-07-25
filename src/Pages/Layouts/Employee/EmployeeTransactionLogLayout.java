package Pages.Layouts.Employee;

import DB.*;
import Model.DAO.*;
import Model.POJO.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeTransactionLogLayout {

    private static final Logger LOGGER = Logger.getLogger(EmployeeTransactionLogLayout.class.getName());
    private static int maxMonths = 1;
    private static int totalDaysLoaded = 0;
    private static final int DAYS_PER_SHOW_MORE = 10;

    /**
     * Builds the main layout for employee transaction logs.
     * Contains title, filter, and scrollable list of daily summaries.
     */
    public static VBox build(BorderPane parentLayout) {
        totalDaysLoaded = 0;

        // Title
        Label title = new Label("Transaction Log");
        title.setId("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(10, 0, 10, 0));

        // Filtering
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Last 1 month", "Last 2 months", "Last 3 months");
        filterBox.setValue("Last 1 month");
        filterBox.getStyleClass().add("inventory-button");
        filterBox.setPrefSize(140, 32);

        // Title + filter row
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleSection = new HBox(10, title, spacer, filterBox);
        titleSection.setAlignment(Pos.CENTER_LEFT);
        titleSection.setPadding(new Insets(0, 0, 5, 0));

        // Daily transaction list
        VBox dayList = new VBox(15);
        dayList.setPadding(new Insets(10));
        dayList.setAlignment(Pos.TOP_CENTER);

        // Footer for "Show More" or end message
        VBox footerBox = new VBox();
        footerBox.setAlignment(Pos.CENTER);
        dayList.getChildren().add(footerBox);

        // Filter selection logic
        filterBox.setOnAction(e -> {
            totalDaysLoaded = 0;
            maxMonths = switch (filterBox.getValue()) {
                case "Last 2 months" -> 2;
                case "Last 3 months" -> 3;
                default -> 1;
            };
            LOGGER.log(Level.INFO, "Filter changed to last {0} month(s)", maxMonths);
            dayList.getChildren().removeIf(node -> node != footerBox);
            footerBox.getChildren().clear();
            loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);
        });

        // Scrollable container
        ScrollPane scrollPane = new ScrollPane(dayList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Root layout
        VBox root = new VBox(10, titleSection, scrollPane);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root-panel");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Load initial data
        loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);

        return root;
    }

    /**
     * Creates a single summary card for a day’s transactions.
     * Displays total sales count and amount for the given date.
     */
    private static VBox createDaySummary(LocalDate date, DateTimeFormatter formatter, List<Sale> sales, BorderPane layout) {
        Label dateLabel = new Label("📅 " + date.format(formatter));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        int totalSalesCount = sales.size();
        Label countLabel = new Label("Total Sales: " + totalSalesCount);
        countLabel.setStyle("-fx-text-fill: #cccccc;");

        double totalSalesAmount = sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
        Label salesLabel = new Label("Total Sales of the Day: " + AppFormatter.formatCurrency(totalSalesAmount));
        salesLabel.setStyle("-fx-text-fill: #cccccc;");

        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("inventory-button");
        viewBtn.setOnAction(e -> {
            LOGGER.log(Level.INFO, "Viewing sales details for date: {0}", date);
            layout.setCenter(EmployeeTransactionLogDetailsLayout.build(layout, date, sales));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, dateLabel, spacer, viewBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, header, countLabel, salesLabel);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");

        return card;
    }

    /**
     * Loads and displays transaction summaries for previous days.
     * Uses filters to limit how far back to fetch transactions.
     */
    private static void loadMoreDays(VBox dayList, BorderPane layout, VBox footerBox, int daysToLoad, boolean respectFilterLimit) {
        List<LocalDate> allDates = SaleDAO.getAllSaleDates();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusMonths(maxMonths);

        int added = 0;
        footerBox.getChildren().clear();

        for (int i = totalDaysLoaded; i < allDates.size() && added < daysToLoad; i++) {
            LocalDate date = allDates.get(i);
            if (respectFilterLimit && date.isBefore(minDate)) break;

            List<Sale> sales = SaleDAO.getSalesByDate(date);
            if (!sales.isEmpty()) {
                VBox card = createDaySummary(date, formatter, sales, layout);
                dayList.getChildren().add(dayList.getChildren().size() - 1, card);
                added++;
                totalDaysLoaded++;
            }
        }

        boolean moreAvailable = totalDaysLoaded < allDates.size();

        if (moreAvailable) {
            Button more = new Button("Show More");
            more.getStyleClass().add("inventory-button");
            more.setOnAction(ev -> loadMoreDays(dayList, layout, footerBox, DAYS_PER_SHOW_MORE, false));
            footerBox.getChildren().add(more);
        } else {
            Label done = new Label("No more transaction records available");
            done.setStyle("-fx-text-fill: #888; -fx-padding: 10 0 20 0;");
            footerBox.getChildren().add(done);
        }

        LOGGER.log(Level.INFO, "Loaded {0} more day(s) of transaction logs", added);
    }

    /**
     * Reloads and refreshes the layout from scratch.
     */
    public static void refresh(BorderPane parentLayout) {
        LOGGER.log(Level.INFO, "Refreshing transaction log layout");
        parentLayout.setCenter(build(parentLayout));
    }
}