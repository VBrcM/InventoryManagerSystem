package Pages.Layouts;

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

    // Entry point to build the main reports layout
    public static VBox build(BorderPane parentLayout) {
        totalDaysLoaded = 0;

        // Title
        Label title = new Label("Sales Reports");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));

        // Filter combo box
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Last 1 month", "Last 2 months", "Last 3 months");
        filterBox.setValue("Last 1 month");
        filterBox.getStyleClass().add("inventory-button");

        // Container for daily summaries
        VBox dayList = new VBox(15);
        dayList.setPadding(new Insets(10));
        dayList.setAlignment(Pos.TOP_CENTER);

        // Footer section for Show More / No records label
        VBox footerBox = new VBox();
        footerBox.setAlignment(Pos.CENTER);
        dayList.getChildren().add(footerBox);

        // Filter change behavior
        filterBox.setOnAction(e -> {
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

        // Layout containers
        HBox filterBar = new HBox(filterBox);
        filterBar.setAlignment(Pos.CENTER_RIGHT);
        filterBar.setPadding(new Insets(0, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(dayList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox root = new VBox(10, title, filterBar, scrollPane);
        root.setPadding(new Insets(30));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);

        return root;
    }

    // Creates a sales summary card for a given date
    private static VBox createDaySummary(LocalDate date, DateTimeFormatter formatter, List<SoldItem> transactions, BorderPane layout) {
        Label dateLabel = new Label("📅 " + date.format(formatter));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        int total = transactions.stream().mapToInt(item -> item.getQuantity() * item.getPrice()).sum();
        Label totalLabel = new Label("Total Sales: ₱" + total);
        totalLabel.setStyle("-fx-text-fill: #cccccc;");

        Button viewBtn = new Button("View Details");
        viewBtn.setOnAction(e -> layout.setCenter(AdminReportDetailsLayout.build(layout, date, transactions)));
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

    // Loads more daily summaries into the layout
    private static void loadMoreDays(VBox dayList, BorderPane layout, VBox footerBox, int daysToLoad, boolean respectFilterLimit) {
        List<LocalDate> allDates = DummyData.getAllTransactionDates();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusMonths(maxMonths);

        int added = 0;
        footerBox.getChildren().clear();

        for (int i = totalDaysLoaded; i < allDates.size() && added < daysToLoad; i++) {
            LocalDate date = allDates.get(i);

            if (respectFilterLimit && date.isBefore(minDate)) break;

            List<SoldItem> transactions = DummyData.getTransactionsForDate(date);
            if (!transactions.isEmpty()) {
                VBox dayCard = createDaySummary(date, formatter, transactions, layout);
                dayList.getChildren().add(dayList.getChildren().size() - 1, dayCard);
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
            Label done = new Label("No more sales records available");
            done.setStyle("-fx-text-fill: #888; -fx-padding: 10 0 20 0;");
            footerBox.getChildren().add(done);
        }
    }

    // Rebuilds the report layout (used for navigation buttons)
    public static void refresh(BorderPane parentLayout) {
        parentLayout.setCenter(build(parentLayout));
    }
}
