package Pages.Layouts;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class EmployeeDashboardLayout {

    // ==========================
    // Main Layout Builder
    // ==========================
    public static VBox build() {
        Label title = new Label("Employee Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 10, 0));

        Label dateLabel = new Label(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");

        VBox todaysSales = createStatBox("Today's Sales", "₱0", Color.web("#4CAF50"));
        VBox transactions = createStatBox("Transactions", "0", Color.web("#2196F3"));
        VBox lowStock = createStatBox("Low Stock", "3 items", Color.web("#F44336"));

        HBox statsRow = new HBox(20, todaysSales, transactions, lowStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(20));
        statsRow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(todaysSales, Priority.ALWAYS);
        HBox.setHgrow(transactions, Priority.ALWAYS);
        HBox.setHgrow(lowStock, Priority.ALWAYS);

        VBox pieChartContainer = new VBox(createPieChart());
        pieChartContainer.setPadding(new Insets(10));
        pieChartContainer.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        VBox.setVgrow(pieChartContainer, Priority.ALWAYS);

        VBox barChartContainer = new VBox(createBarChart());
        barChartContainer.setPadding(new Insets(10));
        barChartContainer.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        VBox.setVgrow(barChartContainer, Priority.ALWAYS);

        VBox leftColumn = new VBox(10, pieChartContainer, barChartContainer);
        leftColumn.setPadding(new Insets(0));
        VBox.setVgrow(leftColumn, Priority.ALWAYS);

        Label recentTitle = new Label("Recent Transactions");
        recentTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        VBox transactionsList = createTransactionsList();
        VBox rightColumn = new VBox(10, recentTitle, transactionsList);
        rightColumn.setPadding(new Insets(10));
        rightColumn.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        VBox.setVgrow(rightColumn, Priority.ALWAYS);

        HBox splitRow = new HBox(20, leftColumn, rightColumn);
        splitRow.setAlignment(Pos.CENTER);
        splitRow.setPadding(new Insets(10, 0, 0, 0));
        splitRow.setMaxWidth(Double.MAX_VALUE);
        splitRow.setMinHeight(500);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        VBox layout = new VBox(20, title, dateLabel, statsRow, splitRow);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #1e1e1e;");
        return layout;
    }

    // ==========================
    // Stat Box
    // ==========================
    private static VBox createStatBox(String labelText, String valueText, Color accentColor) {
        Label value = new Label(valueText);
        value.setStyle(String.format(
                "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: %s;",
                toHexColor(accentColor)
        ));

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");

        VBox box = new VBox(5, value, label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    // ==========================
    // Pie Chart
    // ==========================
    private static PieChart createPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Top Selling Today");
        pieChart.getData().addAll(
                new PieChart.Data("Notebook", 25),
                new PieChart.Data("Pen", 18),
                new PieChart.Data("Marker", 10),
                new PieChart.Data("Stapler", 5)
        );
        pieChart.setLegendVisible(true);
        pieChart.setStyle("-fx-background-color: #2e2e2e;");
        Platform.runLater(() -> {
            Node bg = pieChart.lookup(".chart-plot-background");
            if (bg != null) bg.setStyle("-fx-background-color: transparent;");
        });
        return pieChart;
    }

    // ==========================
    // Bar Chart
    // ==========================
    private static BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Product");
        yAxis.setLabel("Units Sold");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top Selling This Week");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Notebook", 80));
        series.getData().add(new XYChart.Data<>("Pen", 65));
        series.getData().add(new XYChart.Data<>("Binder", 50));
        series.getData().add(new XYChart.Data<>("Tape", 40));

        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: #2e2e2e;");
        return barChart;
    }

    // ==========================
    // Transactions List
    // ==========================
    private static VBox createTransactionsList() {
        VBox transactionsList = new VBox(8);
        transactionsList.setId("transactionsList");

        addTransactionItem(transactionsList, "09:30 AM", "₱250.00", "Notebook (2), Pens (3)");
        addTransactionItem(transactionsList, "10:45 AM", "₱180.50", "Binder (1), Paper (5)");
        addTransactionItem(transactionsList, "11:15 AM", "₱95.00", "Markers (1), Tape (2)");
        return transactionsList;
    }

    private static void addTransactionItem(VBox container, String time, String amount, String items) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 8;");

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 100; -fx-text-fill: #ffffff;");

        Label amountLabel = new Label(amount);
        amountLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-min-width: 100;");

        Label itemsLabel = new Label(items);
        itemsLabel.setStyle("-fx-text-fill: #cccccc;");
        itemsLabel.setWrapText(true);
        HBox.setHgrow(itemsLabel, Priority.ALWAYS);

        item.getChildren().addAll(timeLabel, amountLabel, itemsLabel);
        container.getChildren().add(item);
    }

    // ==========================
    // Utility
    // ==========================
    private static String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
