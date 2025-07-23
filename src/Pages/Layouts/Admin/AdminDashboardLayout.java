package Pages.Layouts.Admin;


import Model.DAO.ProductDAO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import DB.*;
import java.sql.*;
import javafx.scene.paint.Color;


/**
 * AdminDashboardLayout is responsible for rendering the main admin dashboard interface.
 * It includes key metrics (stat boxes), bar charts for recent sales, and a pie chart
 * representing today's sales distribution.
 */
public class AdminDashboardLayout {


    /**
     * Builds the complete admin dashboard layout.
     *
     * @param layout The main BorderPane layout of the app, used for navigation.
     * @return A VBox containing the full dashboard interface.
     */
    public static VBox build(BorderPane layout) {
        ProductDAO productDAO = new ProductDAO();


        // ===== Title Section =====
        Label title = new Label("Admin Dashboard");
        title.setId("title-label");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setPadding(new Insets(10, 0, 20, 0));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);


        // ===== Stat Boxes =====
        VBox totalItems = createStatBox(
                "Total Items",
                Formatter.formatNumber(productDAO.getTotalProducts()),
                Color.web("#4CAF50"),
                () -> layout.setCenter(AdminInventoryLayout.build(false))
        );


        VBox totalStock = createStatBox(
                "Total Stock Value",
                Formatter.formatCurrency(productDAO.getTotalStockValue()),
                Color.web("#4CAF50"),
                null
        );


        VBox outOfStock = createStatBox(
                "Out of Stock",
                Formatter.formatNumber(productDAO.getOutOfStockCount()),
                Color.web("#F44336"),
                () -> layout.setCenter(AdminInventoryLayout.build(true))
        );


        HBox statsRow = new HBox(20, totalItems, totalStock, outOfStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(5, 0, 5, 0));
        HBox.setHgrow(totalItems, Priority.ALWAYS);
        HBox.setHgrow(totalStock, Priority.ALWAYS);
        HBox.setHgrow(outOfStock, Priority.ALWAYS);


        // ===== Chart Section =====
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
        chartsRow.setPadding(new Insets(5, 0, 5, 0));
        chartsRow.setMinHeight(500);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);


        // ===== Final Layout Root =====
        VBox layoutRoot = new VBox(5, title, statsRow, chartsRow);
        layoutRoot.setPadding(new Insets(20));
        layoutRoot.setAlignment(Pos.TOP_CENTER);
        layoutRoot.setStyle("-fx-background-color: #1e1e1e;");


        return layoutRoot;
    }


    /**
     * Creates a styled statistic box (e.g., Total Items).
     *
     * @param labelText  Label text under the number.
     * @param valueText  The actual numeric/statistic value.
     * @param accentColor Color used for value text.
     * @param onClick    Optional click action.
     * @return A styled VBox for the stat box.
     */
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


        if (onClick != null) {
            box.setOnMouseClicked(e -> onClick.run());
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }


        return box;
    }


    /**
     * Converts a JavaFX Color to a hex string.
     */
    private static String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    /**
     * Creates a bar chart for a given dashboard chart title.
     */
    private static BarChart<String, Number> createBarChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle(title);
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(true);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);

        // Force no label rotation
        xAxis.setTickLabelRotation(0);
        xAxis.setStyle("-fx-tick-label-rotation: 0;");

        String sql;
        boolean truncateLabels = false;

        switch (title) {
            case "Sales This Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = buildSalesQuery("MONTH(s.sale_date) = MONTH(CURDATE()) AND YEAR(s.sale_date) = YEAR(CURDATE())");
                truncateLabels = true; // if category names are long
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


    /**
     * Loads bar chart data from SQL result.
     */
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
            e.printStackTrace();
        }


        return series;
    }


    /**
     * Builds SQL for sales by category using a provided WHERE clause.
     */
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


    /**
     * Creates a pie chart showing today's sales distribution by category.
     */
    private static PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Today's Sales");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");


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
            e.printStackTrace();
        }


        // UI adjustments for chart visuals
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


            chart.lookupAll(".chart-legend-item")
                    .forEach(item -> item.setStyle("-fx-text-fill: white;"));


            chart.lookupAll(".chart-pie-label")
                    .forEach(label -> label.setStyle("-fx-fill: white; -fx-font-size: 13px;"));
        });


        return chart;
    }
}