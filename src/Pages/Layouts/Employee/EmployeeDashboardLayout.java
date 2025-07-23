package Pages.Layouts.Employee;

import Model.DAO.*;
import Model.POJO.*;
import Pages.EmployeeAccess;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import static DB.Formatter.formatCurrency;
import static Pages.EmployeeAccess.layout;

public class EmployeeDashboardLayout {

    // Main builder for the Employee Dashboard layout
    public static VBox build() {
        // ===== Dashboard Title =====
        Label title = new Label("Employee Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 10, 0));

        Label dateLabel = new Label(java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.getStyleClass().add("date-label");

        // ===== Stat Boxes =====
        VBox todaysSales = createStatBox(
                "Today's Sales",
                formatCurrency(getTodaySalesTotal()),  // Use the updated method here
                Color.web("#4CAF50"),
                () -> EmployeeAccess.getLayout().setCenter(EmployeeSalesLayout.build(layout))
        );

        VBox transactions = createStatBox(
                "Transactions",
                String.valueOf(TransactionDAO.getTodayCount()),
                Color.web("#2196F3"),
                () -> EmployeeAccess.getLayout().setCenter(EmployeeTransactionLayout.build())
        );

        VBox lowStock = createStatBox(
                "Low Stock",
                ProductDAO.getLowStockCount() + " items",
                Color.web("#F44336"),
                () -> EmployeeAccess.getLayout().setCenter(EmployeeInventoryLayout.build())
        );

        HBox statsRow = new HBox(20, todaysSales, transactions, lowStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(10, 0, 10, 0));
        HBox.setHgrow(todaysSales, Priority.ALWAYS);
        HBox.setHgrow(transactions, Priority.ALWAYS);
        HBox.setHgrow(lowStock, Priority.ALWAYS);

        VBox pieChartContainer = new VBox(createPieChart());
        pieChartContainer.getStyleClass().add("chart-container");
        VBox.setVgrow(pieChartContainer, Priority.ALWAYS);

        VBox barChartContainer = new VBox(createBarChart());
        barChartContainer.getStyleClass().add("chart-container");
        VBox.setVgrow(barChartContainer, Priority.ALWAYS);

        VBox leftColumn = new VBox(10, pieChartContainer, barChartContainer);
        VBox.setVgrow(leftColumn, Priority.ALWAYS);

        Label recentTitle = new Label("Recent Transactions");
        recentTitle.getStyleClass().add("recent-title");

        VBox transactionsList = createTransactionsList();
        VBox rightColumn = new VBox(10, recentTitle, transactionsList);
        rightColumn.getStyleClass().add("chart-container");
        VBox.setVgrow(rightColumn, Priority.ALWAYS);

        HBox splitRow = new HBox(20, leftColumn, rightColumn);
        splitRow.setAlignment(Pos.CENTER);
        splitRow.setPadding(new Insets(5, 0, 0, 0));        splitRow.setMinHeight(500);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        VBox layout = new VBox(10, title, dateLabel, statsRow, splitRow);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #1e1e1e;");

        // Log for debugging
        System.out.println("Today's Sales Total: " + getTodaySalesTotal());  // Debugging log

        return layout;
    }

    // Fetch today's sales total with proper exception handling
    private static double getTodaySalesTotal() {
        double totalSales = SaleDAO.getTodaySalesTotal();
        if (totalSales == 0.0) {
            System.out.println("No sales recorded for today.");  // Debugging log if no sales data is returned
        }
        return totalSales;
    }

    private static VBox createStatBox(String labelText, String valueText, Color accentColor, Runnable onClick) {
        Label value = new Label(valueText);
        value.getStyleClass().add("stat-value");
        value.setStyle(String.format("-fx-text-fill: %s;", toHexColor(accentColor)));

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        VBox box = new VBox(5, value, label);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("stat-box");
        box.setMaxWidth(Double.MAX_VALUE);

        // If clickable, set cursor and click handler
        if (onClick != null) {
            box.setOnMouseClicked((MouseEvent e) -> onClick.run());
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }

        return box;
    }

    private static PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Top Selling Today");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");

        Map<String, Integer> pieData = TransactionDAO.getTodayTransactionSummaryByProduct();

        for (Map.Entry<String, Integer> entry : pieData.entrySet()) {
            String label = entry.getKey();
            if (label.length() > 15) {
                label = label.substring(0, 12) + "...";
            }

            chart.getData().add(new PieChart.Data(label, entry.getValue()));
        }

        Platform.runLater(() -> {
            Node bg = chart.lookup(".chart-plot-background");
            if (bg != null) bg.setStyle("-fx-background-color: transparent;");

            Node legend = chart.lookup(".chart-legend");
            if (legend != null) {
                legend.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 13px;
            """);
            }

            chart.lookupAll(".chart-legend-item").forEach(item -> {
                item.setStyle("-fx-text-fill: white;");
            });

            chart.lookupAll(".chart-pie-label").forEach(labelNode -> {
                labelNode.setStyle("-fx-fill: white; -fx-font-size: 13px;");
            });
        });

        return chart;
    }

    private static BarChart<String, Number> createBarChart() {
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Product");
        yAxis.setLabel("Units Sold");

        // Create bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top Selling This Week");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Selling");

        Map<String, Integer> weeklyData = TransactionDAO.getWeeklyTransactionSummaryByProduct();

        int maxVal = 5;

        for (Map.Entry<String, Integer> entry : weeklyData.entrySet()) {
            String label = entry.getKey();
            int value = entry.getValue();

            // Optionally truncate long product names
            if (label.length() > 12) {
                label = label.substring(0, 10) + "...";
            }

            series.getData().add(new XYChart.Data<>(label, value));

            if (value > maxVal) {
                maxVal = value;
            }
        }

        // Y-axis scaling like in your createChart method
        int upperBound = ((maxVal + 9) / 10) * 10;
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(upperBound / 5.0);

        barChart.getData().add(series);

        // Apply style settings
        barChart.setLegendVisible(false);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(true);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);

        // Aesthetic styling (background, padding, rounded corners)
        barChart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");

        return barChart;
    }

    private static VBox createTransactionsList() {
        VBox transactionsList = new VBox(8);
        transactionsList.setId("transactionsList");

        int maxItems = 12;
        List<Transaction> flatList = TransactionDAO.getRecentTransactions(12); // get more for grouping

        // Group by t_id
        Map<Integer, List<Transaction>> grouped = new LinkedHashMap<>();
        for (Transaction txn : flatList) {
            grouped.computeIfAbsent(txn.getTId(), k -> new ArrayList<>()).add(txn);
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        int count = 0;
        for (Map.Entry<Integer, List<Transaction>> entry : grouped.entrySet()) {
            if (count++ >= maxItems) break;

            List<Transaction> txns = entry.getValue();
            LocalDateTime time = txns.get(0).getTDate();
            double totalAmount = txns.stream().mapToDouble(Transaction::getAmount).sum();

            String formattedTime = time.format(timeFormatter);
            String formattedAmount = formatCurrency(totalAmount);

            StringBuilder description = new StringBuilder();
            for (int i = 0; i < txns.size(); i++) {
                Transaction t = txns.get(i);
                description.append(t.getProductName())
                        .append(" (").append(t.getTQty()).append(")");
                if (i < txns.size() - 1) description.append(", ");
            }

            String descStr = description.toString();
            if (descStr.length() > 70) {
                descStr = descStr.substring(0, 70) + "...";
            }

            addTransactionItem(transactionsList, formattedTime, formattedAmount, descStr);
        }

        return transactionsList;
    }


    private static void addTransactionItem(VBox container, String time, String amount, String items) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("transaction-item");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("transaction-time");

        Label amountLabel = new Label(amount);
        amountLabel.getStyleClass().add("transaction-amount");

        String shortDescription = items.length() > 35 ? items.substring(0, 32) + "..." : items;

        Label itemsLabel = new Label(shortDescription);
        itemsLabel.getStyleClass().add("transaction-items");
        itemsLabel.setWrapText(true);
        HBox.setHgrow(itemsLabel, Priority.ALWAYS);

        // Redirect to today's transaction layout
        item.setOnMouseClicked(e -> EmployeeAccess.getLayout().setCenter(EmployeeTransactionLayout.build()));
        item.setStyle("-fx-cursor: hand;");

        item.getChildren().addAll(timeLabel, amountLabel, itemsLabel);
        container.getChildren().add(item);
    }


    private static String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}