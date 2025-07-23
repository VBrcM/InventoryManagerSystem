package Pages.Layouts;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class AdminDashboardLayout {

    // Main builder for the Admin Dashboard layout
    public static VBox build(BorderPane layout) {
        ProductDAO productDAO = new ProductDAO();

        // ===== Dashboard Title =====
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

        // ===== Charts Section =====
        VBox chart1Container = new VBox(createChart("Sales This Month"));
        VBox chart2Container = new VBox(createChart("Sales Last Month"));
        VBox chart3Container = new VBox(createChart("Most Popular Items"));
        VBox chart4Container = new VBox(createPieChart());

        chart1Container.getStyleClass().add("chart-container");
        chart2Container.getStyleClass().add("chart-container");
        chart3Container.getStyleClass().add("chart-container");
        chart4Container.getStyleClass().add("chart-container");

        VBox leftColumn = new VBox(20, chart1Container, chart2Container);
        VBox rightColumn = new VBox(20, chart3Container, chart4Container);

        chart1Container.setPadding(new Insets(10));
        chart2Container.setPadding(new Insets(10));
        chart3Container.setPadding(new Insets(10));
        chart4Container.setPadding(new Insets(10));

        HBox chartsRow = new HBox(20, leftColumn, rightColumn);
        chartsRow.setAlignment(Pos.CENTER);
        chartsRow.setPadding(new Insets(5, 0, 5, 0));  // bottom padding added here
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        chartsRow.setMinHeight(500);

        // ===== Final Layout =====
        VBox layoutRoot = new VBox();
        layoutRoot.setSpacing(5); // consistent spacing between title, statsRow, and chartsRow
        layoutRoot.setPadding(new Insets(20, 20, 20, 20));
        layoutRoot.setAlignment(Pos.TOP_CENTER);
        layoutRoot.setStyle("-fx-background-color: #1e1e1e;");

        layoutRoot.getChildren().addAll(title, statsRow, chartsRow);

        return layoutRoot;
    }

    // Creates a stat box (tile) with a label, value, and optional click behavior
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
            box.setOnMouseClicked((MouseEvent e) -> onClick.run());
            box.setStyle(box.getStyle() + " -fx-cursor: hand;");
        }

        return box;
    }

    private static String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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
        barChart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");

        String sql;
        switch (title) {
            case "Sales This Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = """
                SELECT c.category_name, SUM(si.si_qty) AS total
                FROM sale_item si
                JOIN product p ON si.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                JOIN sale s ON si.sale_id = s.sale_id
                WHERE MONTH(s.sale_date) = MONTH(CURDATE())
                  AND YEAR(s.sale_date) = YEAR(CURDATE())
                GROUP BY c.category_name
                ORDER BY total DESC
            """;
            }
            case "Sales Last Month" -> {
                xAxis.setLabel("Category");
                yAxis.setLabel("Quantity Sold");
                sql = """
                SELECT c.category_name, SUM(si.si_qty) AS total
                FROM sale_item si
                JOIN product p ON si.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                JOIN sale s ON si.sale_id = s.sale_id
                WHERE MONTH(s.sale_date) = MONTH(CURDATE() - INTERVAL 1 MONTH)
                  AND YEAR(s.sale_date) = YEAR(CURDATE() - INTERVAL 1 MONTH)
                GROUP BY c.category_name
                ORDER BY total DESC
            """;
            }
            case "Most Popular Items" -> {
                xAxis.setLabel("Product");
                yAxis.setLabel("Quantity Sold");
                xAxis.setTickLabelRotation(0); // horizontal

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

        try (Connection conn = JDBC.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int maxVal = 10;

            while (rs.next()) {
                String label = rs.getString(1);  // category or product name
                if (title.equals("Most Popular Items") && label.length() > 12) {
                    label = label.substring(0, 10) + "...";
                }
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
        PieChart chart = new PieChart();
        chart.setTitle("Today's Sales");
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10; -fx-padding: 10;");

        try (Connection conn = JDBC.connect()) {
            if (conn == null) {
                System.out.println("DB connection is null.");
                return chart;
            }

            String query = """
                SELECT c.category_name AS label, SUM(si.si_qty) AS value
                FROM sale s
                JOIN sale_item si ON s.sale_id = si.sale_id
                JOIN product p ON si.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                WHERE DATE(s.sale_date) = CURDATE()
                GROUP BY c.category_name
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String label = rs.getString("label");
                int value = rs.getInt("value");
                chart.getData().add(new PieChart.Data(label, value));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            // Make the chart background inside transparent
            Node bg = chart.lookup(".chart-plot-background");
            if (bg != null) {
                bg.setStyle("-fx-background-color: transparent;");
            }

            // Make legend text white and legend background transparent
            Node legend = chart.lookup(".chart-legend");
            if (legend != null) {
                legend.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-size: 13px;
        """);
            }

            // Make each legend item label white
            chart.lookupAll(".chart-legend-item").forEach(item -> {
                item.setStyle("-fx-text-fill: white;");
            });

            // Make slice labels white
            chart.lookupAll(".chart-pie-label").forEach(label ->
                    label.setStyle("-fx-fill: white; -fx-font-size: 13px;")
            );
        });

        return chart;
    }
}