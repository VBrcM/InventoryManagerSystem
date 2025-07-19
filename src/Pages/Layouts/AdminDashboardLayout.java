package Pages.Layouts;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import DB.*;
import java.sql.*;

public class AdminDashboardLayout {

    public static VBox build() {

        // ===== Dashboard Title =====
        Label title = new Label("Dashboard");
        title.setId("title-label");
        title.setPadding(new Insets(10, 0, 10, 0));

        // ===== Stat Boxes =====
        VBox totalItems = createStatBox("Total Items", "120");
        VBox totalStock = createStatBox("Total Stock Value", "₱250,000");
        VBox outOfStock = createStatBox("Out of Stock", "5");

        HBox statsRow = new HBox(20, totalItems, totalStock, outOfStock);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(20));
        statsRow.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(totalItems, Priority.ALWAYS);
        HBox.setHgrow(totalStock, Priority.ALWAYS);
        HBox.setHgrow(outOfStock, Priority.ALWAYS);
        totalItems.setPrefWidth(0);
        totalStock.setPrefWidth(0);
        outOfStock.setPrefWidth(0);

        // ===== Charts =====
        BarChart<String, Number> chart1 = createChart("Sales This Month");
        BarChart<String, Number> chart2 = createChart("Sales Last Month");
        BarChart<String, Number> chart3 = createChart("Most Popular Items");

        PieChart chart4 = new PieChart();
        chart4.setTitle("Today's Sales");
        chart4.getData().add(new PieChart.Data("Stationery", 45));
        chart4.getData().add(new PieChart.Data("Electronics", 25));
        chart4.getData().add(new PieChart.Data("Snacks", 30));
        chart4.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");

        Platform.runLater(() -> {
            Node bg = chart4.lookup(".chart-plot-background");
            if (bg != null) {
                bg.setStyle("-fx-background-color: transparent;");
            }
        });

        // ===== Chart Grid =====
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        grid.add(chart1, 0, 0);
        grid.add(chart2, 1, 0);
        grid.add(chart3, 0, 1);
        grid.add(chart4, 1, 1);

        for (Node chart : grid.getChildren()) {
            GridPane.setHgrow(chart, Priority.ALWAYS);
            GridPane.setVgrow(chart, Priority.ALWAYS);
        }

        // ===== Final Layout =====
        VBox layout = new VBox(20, title, statsRow, grid);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(grid, Priority.ALWAYS);

        return layout;
    }

    private static BarChart<String, Number> createChart(String title) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(400);
        yAxis.setTickUnit(100);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        chart.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
//-----------------------
        String query;
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        try {
            Connection conn = JDBC.connect();
            if (conn == null) {
                System.out.println("DB connection is null.");
                return chart;
            }

            PreparedStatement stmt;
            ResultSet rs;

            switch (title) {
                case "Sales This Month":
                    query = "SELECT WEEK(s.sale_date, 1) AS week_num, " +
                            "SUM(si.si_qty) AS total_sales " +
                            "FROM sale s " +
                            "JOIN sale_item si ON s.sale_id = si.sale_id " +
                            "WHERE MONTH(s.sale_date) = MONTH(CURDATE()) " +
                            "AND YEAR(s.sale_date) = YEAR(CURDATE()) " +
                            "GROUP BY week_num " +
                            "ORDER BY week_num";
                    break;

                case "Sales Last Month":
                    query = "SELECT WEEK(s.sale_date, 1) AS week_num, " +
                            "SUM(si.si_qty) AS total_sales " +
                            "FROM Sale s " +
                            "JOIN Sale_item si ON s.sale_id = si.sale_id " +
                            "WHERE MONTH(s.sale_date) = MONTH(CURDATE() - INTERVAL 1 MONTH) " +
                            "AND YEAR(s.sale_date) = YEAR(CURDATE() - INTERVAL 1 MONTH) " +
                            "GROUP BY week_num " +
                            "ORDER BY week_num";
                    break;

                case "Most Popular Items":
                    query = "SELECT p.product AS label, SUM(si.si_qty) AS value " +
                            "FROM Product p " +
                            "JOIN Sale_item si ON p.product_id = si.product_id " +
                            "GROUP BY p.product " +
                            "ORDER BY value DESC " +
                            "LIMIT 5";
                    break;

                case "Today's Sales":
                    query = "SELECT c.category AS label, SUM(si.si_qty) AS value " +
                            "FROM Sale s " +
                            "JOIN Sale_item si ON s.sale_id = si.sale_id " +
                            "JOIN Product p ON si.product_id = p.product_id " +
                            "JOIN Category c ON p.category_id = c.category_id " +
                            "WHERE DATE(s.sale_date) = CURDATE() " +
                            "GROUP BY c.category";
                    break;
                default:
                    series.getData().add(new XYChart.Data<>("Placeholder", 10));
                    chart.getData().add(series);
                    return chart;
            }

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String label;
                int value;

                if (title.contains("Sales")) {
                    label = "Week " + rs.getInt("week_num");
                    value = rs.getInt("total_sales");
                } else {
                    label = rs.getString("label");
                    value = rs.getInt("value");
                }

                series.getData().add(new XYChart.Data<>(label, value));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        chart.getData().add(series);

//-------------------------------
        Platform.runLater(() -> {
            Node bg = chart.lookup(".chart-plot-background");
            if (bg != null) {
                bg.setStyle("-fx-background-color: transparent;");
            }
        });

        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        return chart;
    }

    private static VBox createStatBox(String labelText, String valueText) {
        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");

        VBox box = new VBox(5, value, label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 10;");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }
}
