package Pages.Layouts;

import DB.*;
import DB.Formatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EmployeeDashboardLayout {
    public static VBox build(BorderPane layout) {
        System.out.println("[DEBUG] Building EmployeeDashboardLayout...");

        // ===== Title =====
        Label title = new Label("Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 10, 0));

        System.out.println("[DEBUG] Fetching stats for stat boxes...");
        VBox totalProducts = createStatBox(
                "Total Products",
                Formatter.formatNumber(ProductDAO.getTotalProducts()),
                () -> layout.setCenter(EmployeeTransactionLayout.build())
        );

        VBox totalTransactionsToday = createStatBox(
                "Total Transactions Today",
                Formatter.formatNumber(new TransactionDAO().getToday()),
                null
        );

        VBox outOfStock = createStatBox(
                "Out of Stock",
                Formatter.formatNumber(ProductDAO.getOutOfStockCount()),
                () -> layout.setCenter(EmployeeTransactionLayout.build(true))
        );

        HBox row1 = new HBox(20, totalProducts, totalTransactionsToday, outOfStock);
        row1.setAlignment(Pos.CENTER);
        row1.setPadding(new Insets(20));

        HBox.setHgrow(totalProducts, Priority.ALWAYS);
        HBox.setHgrow(totalTransactionsToday, Priority.ALWAYS);
        HBox.setHgrow(outOfStock, Priority.ALWAYS);

        System.out.println("[DEBUG] Fetching today’s items added/reduced...");
        VBox itemsAddedToday = createStatBox(
                "Total Items Added Today",
                Formatter.formatNumber(ProductDAO.getTotalItemsAddedToday()),
                null
        );

        VBox itemsReducedToday = createStatBox(
                "Total Items Reduced Today",
                Formatter.formatNumber(ProductDAO.getTotalItemsReducedToday()),
                null
        );
        HBox row2 = new HBox(20, itemsAddedToday, itemsReducedToday);
        row2.setAlignment(Pos.CENTER);
        row2.setPadding(new Insets(0, 20, 20, 20));

        HBox.setHgrow(itemsAddedToday, Priority.ALWAYS);
        HBox.setHgrow(itemsReducedToday, Priority.ALWAYS);

        System.out.println("[DEBUG] Creating charts...");
        BarChart<String, Number> chart1 = createChart("Transactions This Month by Category");
        BarChart<String, Number> chart2 = createChart("Transactions Today by Category");  // Updated as you requested

        HBox charts = new HBox(20, chart1, chart2);
        charts.setPadding(new Insets(20));
        charts.setAlignment(Pos.CENTER);
        HBox.setHgrow(chart1, Priority.ALWAYS);
        HBox.setHgrow(chart2, Priority.ALWAYS);

        VBox finalLayout = new VBox(20, title, row1, row2, charts);
        finalLayout.setPadding(new Insets(30));
        finalLayout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(charts, Priority.ALWAYS);

        System.out.println("[DEBUG] EmployeeDashboardLayout built successfully.");
        return finalLayout;
    }

    private static VBox createStatBox(String labelText, String valueText, Runnable onClick) {
        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");

        VBox box = new VBox(5, value, label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        box.setMaxWidth(Double.MAX_VALUE);

        if (onClick != null) {
            box.setOnMouseClicked((MouseEvent e) -> {
                System.out.println("[DEBUG] Stat box clicked: " + labelText);
                onClick.run();
            });
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }

        return box;
    }

    private static BarChart<String, Number> createChart(String title) {
        System.out.println("[DEBUG] Creating chart: " + title);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(title);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);

        String sql;
        List<String> categories = new ArrayList<>();

        switch (title) {
            case "Transactions Today by Category" -> {
                sql = """
                SELECT c.category, COUNT(*) AS total
                FROM transaction t
                JOIN product p ON t.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                WHERE DATE(t.trans_date) = CURDATE()
                GROUP BY c.category
                ORDER BY total DESC
                """;

                xAxis.setLabel("Category");
                xAxis.setTickLabelRotation(0);
                yAxis.setLabel("Total Transactions");
            }
            case "Transactions This Month by Category" -> {
                sql = """
                SELECT c.category, COUNT(*) AS total
                FROM transaction t
                JOIN product p ON t.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                WHERE MONTH(t.trans_date) = MONTH(CURDATE())
                  AND YEAR(t.trans_date) = YEAR(CURDATE())
                GROUP BY c.category
                ORDER BY total DESC
                """;

                xAxis.setLabel("Category");
                xAxis.setTickLabelRotation(0);
                yAxis.setLabel("Total Transactions");
            }
            default -> {
                System.out.println("[DEBUG] Invalid chart type requested: " + title);
                barChart.setTitle("Invalid Chart Type");
                return barChart;
            }
        }

        System.out.println("[DEBUG] SQL Query: " + sql);

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int maxVal = 10;

            while (rs.next()) {
                String label = rs.getString(1);
                int count = rs.getInt(2);
                System.out.println("[DEBUG] Chart Data - Label: " + label + ", Count: " + count);
                categories.add(label);
                series.getData().add(new XYChart.Data<>(label, count));
                if (count > maxVal) maxVal = count;
            }

            int upperBound = ((maxVal + 9) / 10) * 10;
            yAxis.setAutoRanging(false);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(upperBound / 5.0);

            xAxis.setCategories(FXCollections.observableArrayList(categories));

        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Exception during chart data fetch");
            e.printStackTrace();
        }

        barChart.getData().add(series);
        barChart.setLegendVisible(false);

        // Style options
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(true);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);

        System.out.println("[DEBUG] Chart created: " + title);
        return barChart;
    }
}
