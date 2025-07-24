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

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        // 1. Today's Sales — no action
        VBox todaysSales = createStatBox(
                "Today's Sales",
                formatCurrency(getTodaySalesTotal()),
                Color.web("#4CAF50"),
                null // No click action
        );

        VBox transactions = createStatBox(
                "Sales Today",
                NumberFormat.getIntegerInstance(Locale.getDefault()).format(SaleDAO.getSalesByDate(LocalDate.now()).size()),
                Color.web("#2196F3"),
                () -> EmployeeAccess.getLayout().setCenter(EmployeeTransactionLayout.build())
        );

        VBox lowStock = createStatBox(
                "Low/Out of Stock",
                NumberFormat.getIntegerInstance(Locale.getDefault()).format(ProductDAO.getLowStockCount()) + " items",
                Color.web("#F44336"),
                () -> EmployeeAccess.getLayout().setCenter(EmployeeInventoryLayout.build(true))
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
        rightColumn.setMinWidth(500);
        rightColumn.setMaxWidth (500);
        rightColumn.getStyleClass().add("chart-container");
        VBox.setVgrow(rightColumn, Priority.ALWAYS);

        HBox splitRow = new HBox(20, leftColumn, rightColumn);
        splitRow.setAlignment(Pos.CENTER);
        splitRow.setPadding(new Insets(5, 0, 0, 0));
        splitRow.setMinHeight(500);
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

        Map<String, Integer> pieData = new HashMap<>();
        try {
            List<SaleItem> todayItems = SaleItemDAO.getSaleItemsByDate(LocalDate.now());
            for (SaleItem item : todayItems) {
                String label = item.getProductName();
                pieData.put(label, pieData.getOrDefault(label, 0) + item.getSiQty());
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with logger if needed
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

            Node legend = chart.lookup(".chart-legend");
            if (legend != null) {
                legend.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 13px;
            """);
            }

            chart.lookupAll(".chart-legend-item").forEach(item -> item.setStyle("-fx-text-fill: white;"));
            chart.lookupAll(".chart-pie-label").forEach(labelNode -> labelNode.setStyle("-fx-fill: white; -fx-font-size: 13px;"));
        });

        return chart;
    }


    private static BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Product");
        yAxis.setLabel("Units Sold");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top Selling This Week");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Selling");

        Map<String, Integer> weeklyData = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        int maxVal = 5;

        try {
            for (LocalDate d = weekStart; !d.isAfter(today); d = d.plusDays(1)) {
                for (SaleItem item : SaleItemDAO.getSaleItemsByDate(d)) {
                    String name = item.getProductName();
                    int qty = item.getSiQty();
                    weeklyData.put(name, weeklyData.getOrDefault(name, 0) + qty);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Replace with logger if desired
        }

        for (Map.Entry<String, Integer> entry : weeklyData.entrySet()) {
            String label = entry.getKey();
            int value = entry.getValue();

            if (label.length() > 12) {
                label = label.substring(0, 10) + "...";
            }

            series.getData().add(new XYChart.Data<>(label, value));
            if (value > maxVal) maxVal = value;
        }

        // Scale y-axis
        int upperBound = ((maxVal + 9) / 10) * 10;
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(upperBound / 5.0);

        barChart.getData().add(series);

        // Chart style
        barChart.setLegendVisible(false);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(true);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);
        barChart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");

        return barChart;
    }

    //RECENT TRANSACTION
    private static VBox createTransactionsList() {
        VBox transactionsList = new VBox(8);
        transactionsList.setId("transactionsList");

        List<Integer> recentSaleIds = SaleDAO.getRecentSaleIds();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        int count = 0;
        int maxItems = 14;

        for (Integer saleId : recentSaleIds) {
            if (count >= maxItems) break;

            Sale sale = SaleDAO.getSaleById(saleId);
            List<SaleItem> items = SaleItemDAO.getSaleItemsBySaleId(saleId);

            if (sale == null || items == null || items.isEmpty()) continue;

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
            transactionsList.getChildren().add(row);

            count++;
        }

        return transactionsList;
    }


    private static String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}