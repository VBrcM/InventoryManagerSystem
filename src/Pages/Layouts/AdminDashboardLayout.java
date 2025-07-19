package Pages.Layouts;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

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

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        switch (title) {
            case "Sales This Month":
                series.getData().add(new XYChart.Data<>("Week 1", 120));
                series.getData().add(new XYChart.Data<>("Week 2", 95));
                series.getData().add(new XYChart.Data<>("Week 3", 140));
                series.getData().add(new XYChart.Data<>("Week 4", 110));
                break;
            case "Sales Last Month":
                series.getData().add(new XYChart.Data<>("Week 1", 100));
                series.getData().add(new XYChart.Data<>("Week 2", 130));
                series.getData().add(new XYChart.Data<>("Week 3", 90));
                series.getData().add(new XYChart.Data<>("Week 4", 115));
                break;
            case "Most Popular Items":
                series.getData().add(new XYChart.Data<>("Pens", 300));
                series.getData().add(new XYChart.Data<>("Notebooks", 250));
                series.getData().add(new XYChart.Data<>("Folders", 180));
                series.getData().add(new XYChart.Data<>("Markers", 120));
                break;
            default:
                series.getData().add(new XYChart.Data<>("Placeholder", 10));
                break;
        }

        chart.getData().add(series);

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
