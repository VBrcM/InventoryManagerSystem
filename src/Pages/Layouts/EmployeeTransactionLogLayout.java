package Pages.Layouts;

import Model.DAO.TransactionDAO;
import Model.POJO.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeTransactionLogLayout {
    private static int maxMonths = 1;
    private static int totalDaysLoaded = 0;
    private static final int DAYS_PER_LOAD = 10;

    public static VBox build(BorderPane parentLayout) {
        totalDaysLoaded = 0;
        System.out.println("[DEBUG] Building Transaction Log layout. maxMonths=" + maxMonths);

        Label title = new Label("Transaction Log");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Last 1 month", "Last 2 months", "Last 3 months");
        filterBox.setValue("Last 1 month");
        filterBox.getStyleClass().add("inventory-button");

        VBox dayList = new VBox(15);
        dayList.setPadding(new Insets(10));
        dayList.setAlignment(Pos.TOP_CENTER);

        VBox footerBox = new VBox();
        footerBox.setAlignment(Pos.CENTER);
        dayList.getChildren().add(footerBox);

        filterBox.setOnAction(e -> {
            totalDaysLoaded = 0;
            maxMonths = switch (filterBox.getValue()) {
                case "Last 1 month" -> 1;
                case "Last 2 months" -> 2;
                case "Last 3 months" -> 3;
                default -> 1;
            };
            System.out.println("[DEBUG] Filter changed to: " + filterBox.getValue() + ", maxMonths set to " + maxMonths);
            dayList.getChildren().removeIf(node -> node != footerBox);
            footerBox.getChildren().clear();
            loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);
        });

        HBox filterBar = new HBox(filterBox);
        filterBar.setAlignment(Pos.CENTER_RIGHT);
        filterBar.setPadding(new Insets(0, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(dayList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");

        VBox root = new VBox(10, title, filterBar, scrollPane);
        root.setPadding(new Insets(30));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadMoreDays(dayList, parentLayout, footerBox, Integer.MAX_VALUE, true);

        return root;
    }

    private static VBox createDaySummary(LocalDate date, DateTimeFormatter formatter, List<Transaction> logs, BorderPane layout) {
        System.out.println("[DEBUG] Creating day summary for date: " + date + " with " + logs.size() + " transactions");
        Label dateLabel = new Label("📅 " + date.format(formatter));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        int total = logs.size();
        Label totalLabel = new Label("Total Transactions: " + total);
        totalLabel.setStyle("-fx-text-fill: #cccccc;");

        Button viewBtn = new Button("View Details");
        viewBtn.setOnAction(e -> {
            System.out.println("[DEBUG] View Details clicked for date: " + date);
            layout.setCenter(EmployeeTransactionLogDetailsLayout.build(layout, date, logs));
        });
        viewBtn.getStyleClass().add("inventory-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, dateLabel, spacer, viewBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, header, totalLabel);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");

        return card;
    }

    private static void loadMoreDays(VBox dayList, BorderPane layout, VBox footerBox, int daysToLoad, boolean respectLimit) {
        System.out.println("[DEBUG] loadMoreDays called. totalDaysLoaded=" + totalDaysLoaded + ", daysToLoad=" + daysToLoad + ", respectLimit=" + respectLimit);

        List<LocalDate> allDates = TransactionDAO.getAllTransactionDates();
        System.out.println("[DEBUG] Retrieved " + allDates.size() + " total transaction dates from DB.");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusMonths(maxMonths);
        System.out.println("[DEBUG] minDate cutoff is " + minDate);

        int added = 0;
        footerBox.getChildren().clear();

        for (int i = totalDaysLoaded; i < allDates.size() && added < daysToLoad; i++) {
            LocalDate date = allDates.get(i);
            System.out.println("[DEBUG] Considering date: " + date);

            if (respectLimit && date.isBefore(minDate)) {
                System.out.println("[DEBUG] Date " + date + " is before minDate, stopping load.");
                break;
            }

            List<Transaction> logs = TransactionDAO.getTransactionsByDate(date);
            System.out.println("[DEBUG] Found " + logs.size() + " transactions on " + date);

            if (!logs.isEmpty()) {
                VBox card = createDaySummary(date, formatter, logs, layout);
                dayList.getChildren().add(dayList.getChildren().size() - 1, card);
                added++;
                totalDaysLoaded++;
                System.out.println("[DEBUG] Added summary card for " + date + ". totalDaysLoaded now " + totalDaysLoaded);
            }
        }

        boolean moreAvailable = totalDaysLoaded < allDates.size();
        System.out.println("[DEBUG] More available: " + moreAvailable);

        if (moreAvailable) {
            Button more = new Button("Show More");
            more.getStyleClass().add("inventory-button");
            more.setOnAction(ev -> {
                System.out.println("[DEBUG] Show More button clicked");
                loadMoreDays(dayList, layout, footerBox, DAYS_PER_LOAD, false);
            });
            footerBox.getChildren().add(more);
        } else {
            Label done = new Label("No more transaction records available");
            done.setStyle("-fx-text-fill: #888;");
            footerBox.getChildren().add(done);
            System.out.println("[DEBUG] No more transaction records available.");
        }
    }

    public static void refresh(BorderPane parentLayout) {
        System.out.println("[DEBUG] Refreshing transaction log layout");
        parentLayout.setCenter(build(parentLayout));
    }
}

