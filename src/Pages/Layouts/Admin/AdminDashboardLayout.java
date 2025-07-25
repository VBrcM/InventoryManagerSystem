package Pages.Layouts.Admin;

import DB.*;
import Model.DAO.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds the admin dashboard interface with stats and charts.
 * Pulls product and sales data to display trends and inventory levels.
 */
public class AdminDashboardLayout {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardLayout.class.getName());

    // Builds the full dashboard layout
    public static VBox build(BorderPane layout) {
        Label title = new Label("Admin Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 20, 0));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // Stat: Total items
        VBox totalItems = createStatBox("Total Items", AppFormatter.formatNumber(ProductDAO.getTotalProducts()),
                () -> layout.setCenter(AdminInventoryLayout.build(false)));
        totalItems.getStyleClass().add("statbox-count");

        // Stat: Total stock value
        VBox totalStock = createStatBox("Total Stock Value", AppFormatter.formatCurrency(ProductDAO.getTotalStockValue()), null);
        totalStock.getStyleClass().add("statbox-sales");

        // Stat: Low/Out of stock
        VBox outOfStock = createStatBox("Low/Out of Stock", AppFormatter.formatNumber(ProductDAO.getOutOfStockCount()),
                () -> layout.setCenter(AdminInventoryLayout.build(true)));
        outOfStock.getStyleClass().add("statbox-nostock");

        HBox statsRow = new HBox(20, totalItems, totalStock, outOfStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(5));
        HBox.setHgrow(totalItems, Priority.ALWAYS);
        HBox.setHgrow(totalStock, Priority.ALWAYS);
        HBox.setHgrow(outOfStock, Priority.ALWAYS);

        // Charts
        VBox chart1Container = new VBox(createBarChart("Sales This Month"));
        VBox chart2Container = new VBox(createBarChart("Sales Last Month"));
        VBox chart3Container = new VBox(createBarChart("Most Popular Items"));
        VBox chart4Container = new VBox(createPieChart());

        for (VBox chart : new VBox[]{chart1Container, chart2Container, chart3Container, chart4Container}) {
            chart.getStyleClass().add("chart-container");
            chart.setPadding(new Insets(10));
        }

        VBox leftColumn = new VBox(20, chart1Container, chart2Container);
        VBox rightColumn = new VBox(20, chart3Container, chart4Container);

        HBox chartsRow = new HBox(20, leftColumn, rightColumn);
        chartsRow.setAlignment(Pos.CENTER);
        chartsRow.setPadding(new Insets(5));
        chartsRow.setMinHeight(500);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        VBox layoutRoot = new VBox(5, title, statsRow, chartsRow);
        layoutRoot.getStyleClass().add("root-panel");
        layoutRoot.setAlignment(Pos.TOP_CENTER);

        return layoutRoot;
    }

    // Creates a single statistic box with optional click action
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
            box.setOnMouseClicked(e -> onClick.run());
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }

        return box;
    }

    // Creates a bar chart based on chart title
    private static BarChart<String, Number> createBarChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle(title);
        barChart.setLegendVisible(false);
        barChart.getStyleClass().add("dashboard-bar-chart");
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(true);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        xAxis.setTickLabelRotation(0);

        String sql;
        boolean truncateLabels = false;

        switch (title) {
            case "Sales This Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = buildSalesQuery("MONTH(s.sale_date) = MONTH(CURDATE()) AND YEAR(s.sale_date) = YEAR(CURDATE())");
                truncateLabels = true;
            }
            case "Sales Last Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = buildSalesQuery("MONTH(s.sale_date) = MONTH(CURDATE() - INTERVAL 1 MONTH) AND YEAR(s.sale_date) = YEAR(CURDATE() - INTERVAL 1 MONTH)");
            }
            case "Most Popular Items" -> {
                xAxis.setLabel("Product");
                yAxis.setLabel("Quantity Sold");
                truncateLabels = true;
                sql = """
                   SELECT p.product_name, SUM(si.si_qty) AS total
                   FROM sale_item si
                   JOIN product p ON si.product_id = p.product_id
                   JOIN sale s ON si.sale_id = s.sale_id
                   WHERE YEAR(s.sale_date) = YEAR(CURDATE())
                   GROUP BY p.product_name
                   ORDER BY total DESC
                   LIMIT 5
               """;
            }
            default -> {
                LOGGER.warning("Invalid chart title provided: " + title);
                barChart.setTitle("Invalid Chart Type");
                return barChart;
            }
        }

        XYChart.Series<String, Number> series = loadChartData(sql, truncateLabels);

        if (!series.getData().isEmpty()) {
            int max = series.getData().stream()
                    .mapToInt(d -> d.getYValue().intValue())
                    .max()
                    .orElse(10);
            int upperBound = ((max + 9) / 10) * 10;
            yAxis.setAutoRanging(false);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(upperBound / 5.0);
        }

        barChart.getData().add(series);
        return barChart;
    }

    // Loads chart data from SQL query
    private static XYChart.Series<String, Number> loadChartData(String sql, boolean truncateLabel) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String label = rs.getString(1);
                if (truncateLabel && label.length() > 12) {
                    label = label.substring(0, 10) + "...";
                }
                int value = rs.getInt(2);
                series.getData().add(new XYChart.Data<>(label, value));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading chart data", e);
        }

        return series;
    }

    // Constructs dynamic sales SQL based on given where clause
    private static String buildSalesQuery(String whereClause) {
        return String.format("""
           SELECT c.category_name, SUM(si.si_qty) AS total
           FROM sale_item si
           JOIN product p ON si.product_id = p.product_id
           JOIN category c ON p.category_id = c.category_id
           JOIN sale s ON si.sale_id = s.sale_id
           WHERE %s
           GROUP BY c.category_name
           ORDER BY total DESC
           LIMIT 5
       """, whereClause);
    }

    // Creates pie chart for today's category sales
    private static PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Today's Sales");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(false);
        chart.setLegendSide(Side.BOTTOM);
        chart.getStyleClass().add("dashboard-pie-chart");

        String sql = """
          SELECT c.category_name AS label, SUM(si.si_qty) AS value
          FROM sale s
          JOIN sale_item si ON s.sale_id = si.sale_id
          JOIN product p ON si.product_id = p.product_id
          JOIN category c ON p.category_id = c.category_id
          WHERE DATE(s.sale_date) = CURDATE()
          GROUP BY c.category_name
       """;

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                chart.getData().add(new PieChart.Data(rs.getString("label"), rs.getInt("value")));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading pie chart data", e);
        }

        // Apply styling after chart is rendered
        Platform.runLater(() -> {
            Node bg = chart.lookup(".chart-plot-background");
            if (bg != null) {
                bg.setStyle("-fx-background-color: transparent;");
            }

            Node legend = chart.lookup(".chart-legend");
            if (legend != null) {
                legend.setStyle("""
                  -fx-background-color: transparent;
                  -fx-text-fill: white;
                  -fx-font-size: 13px;
               """);
            }

            chart.lookupAll(".chart-legend-item").forEach(item ->
                    item.setStyle("-fx-text-fill: white;"));

            chart.lookupAll(".chart-pie-label").forEach(label ->
                    label.setStyle("-fx-fill: white; -fx-font-size: 13px;"));
        });

        return chart;
    }
}