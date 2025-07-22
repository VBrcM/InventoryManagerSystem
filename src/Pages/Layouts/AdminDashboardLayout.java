package Pages.Layouts;

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
import javafx.scene.input.MouseEvent;

public class AdminDashboardLayout {

    // Main builder for the Admin Dashboard layout
    public static VBox build(BorderPane layout) {

        // ===== Dashboard Title =====
        Label title = new Label("Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 10, 0));

        // ===== Stat Boxes =====
        VBox totalItems = createStatBox("Total Items", String.valueOf(ProductDAO.getTotalProducts()),  () -> layout.setCenter(AdminInventoryLayout.build(false)));
        VBox totalStock = createStatBox("Total Stock Value", Formatter.formatCurrency(ProductDAO.getTotalStockValue()), null);

        // Out of stock stat box redirects to filtered inventory view when clicked
        VBox outOfStock = createStatBox("Out of Stock", String.valueOf(ProductDAO.getOutOfStockCount()), () -> layout.setCenter(AdminInventoryLayout.build(true)));

        // Horizontal container for all stat boxes
        HBox statsRow = new HBox(20, totalItems, totalStock, outOfStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(20));
        statsRow.setMaxWidth(Double.MAX_VALUE);

        // Expand boxes to fill space equally
        HBox.setHgrow(totalItems, Priority.ALWAYS);
        HBox.setHgrow(totalStock, Priority.ALWAYS);
        HBox.setHgrow(outOfStock, Priority.ALWAYS);
        totalItems.setPrefWidth(0);
        totalStock.setPrefWidth(0);
        outOfStock.setPrefWidth(0);

        // Create four charts for dashboard
        BarChart<String, Number> chart1 = createChart("Sales This Month");
        BarChart<String, Number> chart2 = createChart("Sales Last Month");
        BarChart<String, Number> chart3 = createChart("Most Popular Items");
        PieChart chart4 = createPieChart();

        // Position charts in a grid layout
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        grid.add(chart1, 0, 0);
        grid.add(chart2, 1, 0);
        grid.add(chart3, 0, 1);
        grid.add(chart4, 1, 1);

        // Allow charts to grow inside grid cells
        for (Node chart : grid.getChildren()) {
            GridPane.setHgrow(chart, Priority.ALWAYS);
            GridPane.setVgrow(chart, Priority.ALWAYS);
        }

        // Final vertical layout
        VBox finalLayout = new VBox(20, title, statsRow, grid);
        finalLayout.setPadding(new Insets(30));
        finalLayout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(grid, Priority.ALWAYS);

        return finalLayout;
    }

    // Creates a stat box (tile) with a label, value, and optional click behavior
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

        // If clickable, set cursor and click handler
        if (onClick != null) {
            box.setOnMouseClicked((MouseEvent e) -> onClick.run());
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }

        return box;
    }

    // Creates a bar chart depending on the given title logic
    private static BarChart<String, Number> createChart(String title) {
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Value");

        // Create bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(title);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);

        String sql;
        switch (title) {
            case "Sales This Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = """
                SELECT c.category, SUM(si.si_qty) AS total
                FROM sale_item si
                JOIN product p ON si.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                JOIN sale s ON si.sale_id = s.sale_id
                WHERE MONTH(s.sale_date) = MONTH(CURDATE())
                  AND YEAR(s.sale_date) = YEAR(CURDATE())
                GROUP BY c.category
                ORDER BY total DESC
            """;
            }
            case "Sales Last Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = """
                SELECT c.category, SUM(si.si_qty) AS total
                FROM sale_item si
                JOIN product p ON si.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                JOIN sale s ON si.sale_id = s.sale_id
                WHERE MONTH(s.sale_date) = MONTH(CURDATE() - INTERVAL 1 MONTH)
                  AND YEAR(s.sale_date) = YEAR(CURDATE() - INTERVAL 1 MONTH)
                GROUP BY c.category
                ORDER BY total DESC
            """;
            }
            case "Most Popular Items" -> {
                xAxis.setLabel("Product");
                yAxis.setLabel("Quantity Sold");
                sql = """
                SELECT p.product, SUM(si.si_qty) AS total
                FROM sale_item si
                JOIN product p ON si.product_id = p.product_id
                JOIN sale s ON si.sale_id = s.sale_id
                WHERE YEAR(s.sale_date) = YEAR(CURDATE())
                GROUP BY p.product
                ORDER BY total DESC
                LIMIT 10
            """;
            }
            default -> {
                barChart.setTitle("Invalid Chart Type");
                return barChart;
            }
        }

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int maxVal = 10;

            while (rs.next()) {
                String label = rs.getString(1);  // category or product name
                int value = rs.getInt(2);        // quantity
                series.getData().add(new XYChart.Data<>(label, value));

                if (value > maxVal) {
                    maxVal = value;
                }
            }

            // Adjust Y-axis to nearest 10 above max value
            int upperBound = ((maxVal + 9) / 10) * 10;
            yAxis.setAutoRanging(false);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(upperBound / 5.0);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        barChart.getData().add(series);
        barChart.setLegendVisible(false);

        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(true);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);

        return barChart;
    }

    // Creates a pie chart showing today's sales grouped by category
    private static PieChart createPieChart() {
        // Create PieChart and set basic styling
        PieChart chart = new PieChart();
        chart.setTitle("Today's Sales");
        chart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);

        try (Connection conn = JDBC.connect()) {
            if (conn == null) {
                System.out.println("DB connection is null.");
                return chart;
            }

            String query = """
            SELECT c.category AS label, SUM(si.si_qty) AS value
            FROM sale s
            JOIN sale_item si ON s.sale_id = si.sale_id
            JOIN product p ON si.product_id = p.product_id
            JOIN category c ON p.category_id = c.category_id
            WHERE DATE(s.sale_date) = CURDATE()
            GROUP BY c.category
        """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Populate chart data
            while (rs.next()) {
                String label = rs.getString("label");
                int value = rs.getInt("value");
                System.out.println("PieChart → " + label + ": " + value); // Debug
                chart.getData().add(new PieChart.Data(label, value));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Remove background inside pie (optional visual polish)
        Platform.runLater(() -> {
            Node bg = chart.lookup(".chart-plot-background");
            if (bg != null) {
                bg.setStyle("-fx-background-color: transparent;");
            }
        });

        return chart;
    }
}