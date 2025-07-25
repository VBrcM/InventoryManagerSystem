package Pages.Layouts.Employee;

import Dialogs.*;
import Model.DAO.*;
import Model.POJO.*;
import Pages.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static DB.AppFormatter.formatCurrency;

/**
 * Provides the layout for the employee dashboard view including sales stats,
 * charts, and recent transactions.
 */
public class EmployeeDashboardLayout {

    private static final Logger LOGGER = Logger.getLogger(EmployeeDashboardLayout.class.getName());

    /**
     * Builds the employee dashboard layout.
     */
    public static VBox build() {
        Label title = new Label("Employee Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.getStyleClass().add("date-label");

        VBox todaysSales = createStatBox(
                "Today's Sales",
                formatCurrency(getTodaySalesTotal()),
                null
        );
        todaysSales.getStyleClass().add("statbox-sales");

        VBox transactions = createStatBox(
                "Sales Today",
                NumberFormat.getIntegerInstance(Locale.getDefault()).format(SaleDAO.getSalesByDate(LocalDate.now()).size()),
                () -> EmployeeAccess.getLayout().setCenter(EmployeeTransactionLayout.build())
        );
        transactions.getStyleClass().add("statbox-count");

        VBox lowStock = createStatBox(
                "Low/Out of Stock",
                NumberFormat.getIntegerInstance(Locale.getDefault()).format(ProductDAO.getLowStockCount()) + " items",
                () -> EmployeeAccess.getLayout().setCenter(EmployeeInventoryLayout.build(true))
        );
        lowStock.getStyleClass().add("statbox-nostock");

        HBox statsRow = new HBox(20, todaysSales, transactions, lowStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(5));
        HBox.setHgrow(todaysSales, Priority.ALWAYS);
        HBox.setHgrow(transactions, Priority.ALWAYS);
        HBox.setHgrow(lowStock, Priority.ALWAYS);

        // Pie and bar chart layout
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
        rightColumn.setMinWidth(500);
        rightColumn.setMaxWidth(500);
        rightColumn.getStyleClass().add("chart-container");
        VBox.setVgrow(rightColumn, Priority.ALWAYS);

        HBox splitRow = new HBox(20, leftColumn, rightColumn);
        splitRow.setAlignment(Pos.CENTER);
        splitRow.setPadding(new Insets(5, 0, 0, 0));
        splitRow.setMinHeight(500);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        VBox root = new VBox(5, title, dateLabel, statsRow, splitRow);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-panel");
        root.setAlignment(Pos.TOP_CENTER);

        LOGGER.info("Employee dashboard loaded successfully");

        return root;
    }

    /**
     * Returns today's total sales value.
     */
    private static double getTodaySalesTotal() {
        double totalSales = 0.0;
        try {
            totalSales = SaleDAO.getTodaySalesTotal();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching today's sales total", e);
        }
        return totalSales;
    }

    /**
     * Creates a statistic display box.
     */
    private static VBox createStatBox(String labelText, String valueText, Runnable onClick) {
        Label value = new Label(valueText);
        value.getStyleClass().add("stat-value");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        VBox box = new VBox(5, value, label);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("stat-box");
        box.setMaxWidth(Double.MAX_VALUE);

        if (onClick != null) {
            box.setOnMouseClicked((MouseEvent e) -> onClick.run());
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }

        return box;
    }

    /**
     * Builds a pie chart of today's top-selling items.
     */
    private static PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Top Selling Today");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(false);
        chart.getStyleClass().add("dashboard-pie-chart");

        Map<String, Integer> pieData = new HashMap<>();
        try {
            List<SaleItem> todayItems = SaleItemDAO.getSaleItemsByDate(LocalDate.now());
            for (SaleItem item : todayItems) {
                String label = item.getProductName();
                pieData.put(label, pieData.getOrDefault(label, 0) + item.getSiQty());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch pie chart data", e);
        }

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
            chart.lookupAll(".chart-pie-label").forEach(labelNode ->
                    labelNode.setStyle("-fx-fill: white; -fx-font-size: 13px;"));
        });

        return chart;
    }

    /**
     * Builds a bar chart of the week's top-selling products.
     */
    private static BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Product");
        yAxis.setLabel("Units Sold");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top Selling This Week");
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Integer> weeklyData = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        int maxVal = 5;

        try {
            for (LocalDate d = weekStart; !d.isAfter(today); d = d.plusDays(1)) {
                for (SaleItem item : SaleItemDAO.getSaleItemsByDate(d)) {
                    String name = item.getProductName();
                    weeklyData.put(name, weeklyData.getOrDefault(name, 0) + item.getSiQty());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch weekly sales data", e);
        }

        int count = 0;
        for (Map.Entry<String, Integer> entry : weeklyData.entrySet()) {
            if (count >= 10) break;
            String label = entry.getKey();
            int value = entry.getValue();
            if (label.length() > 12) {
                label = label.substring(0, 10) + "...";
            }
            series.getData().add(new XYChart.Data<>(label, value));
            maxVal = Math.max(maxVal, value);
            count++;
        }

        int upperBound = ((maxVal + 9) / 10) * 10;
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(upperBound / 5.0);

        barChart.getData().add(series);
        return barChart;
    }

    /**
     * Returns a list of recent sales made today.
     */
    private static VBox createTransactionsList() {
        VBox transactionsList = new VBox(8);
        transactionsList.setId("transactionsList");

        List<Integer> recentSaleIds = SaleDAO.getRecentSaleIds();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        int count = 0;
        int maxItems = 14;
        LocalDate today = LocalDate.now();

        for (Integer saleId : recentSaleIds) {
            if (count >= maxItems) break;

            Sale sale = SaleDAO.getSaleById(saleId);
            if (sale == null || !sale.getSaleDate().toLocalDate().equals(today)) continue;

            List<SaleItem> items = SaleItemDAO.getSaleItemsBySaleId(saleId);
            if (items == null || items.isEmpty()) continue;

            String formattedTime = sale.getSaleDate().format(timeFormatter);
            String formattedAmount = formatCurrency(sale.getTotalAmount());

            StringBuilder itemSummary = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                SaleItem item = items.get(i);
                itemSummary.append(item.getProductName())
                        .append(" (").append(item.getSiQty()).append(")");
                if (i < items.size() - 1) itemSummary.append(", ");
            }

            String summary = itemSummary.toString();
            if (summary.length() > 45) {
                summary = summary.substring(0, 45) + "...";
            }

            HBox row = new HBox(10);
            row.setPadding(new Insets(8));
            row.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 8;");
            row.setAlignment(Pos.CENTER_LEFT);

            Label timeLabel = new Label(formattedTime);
            timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 70;");

            Label amountLabel = new Label(formattedAmount);
            amountLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-weight: bold; -fx-pref-width: 100;");

            Label summaryLabel = new Label(summary);
            summaryLabel.setStyle("-fx-text-fill: white;");
            summaryLabel.setWrapText(true);
            summaryLabel.setMaxWidth(400);

            row.getChildren().addAll(timeLabel, amountLabel, summaryLabel);

            row.setOnMouseClicked(e -> ReceiptDialog.showContent(sale.getSaleDate(), items, items.size()));
            row.setOnMouseEntered(e -> row.setCursor(Cursor.HAND));

            transactionsList.getChildren().add(row);
            count++;
        }

        return transactionsList;
    }
}