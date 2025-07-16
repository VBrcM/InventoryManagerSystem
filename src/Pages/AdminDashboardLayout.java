package Pages.Layouts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDashboardLayout {

    public static VBox build() {
        // --- Title ---
        Label title = new Label("Admin Dashboard");
        title.setId("title-label");

        // --- Bar Chart Setup ---
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Product Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Units Sold");

        BarChart<String, Number> salesChart = new BarChart<>(xAxis, yAxis);
        salesChart.setTitle("Sales Overview");
        salesChart.setPrefHeight(300);
        salesChart.setLegendVisible(false);

        // --- Populate Chart with Sales Data ---
        XYChart.Series<String, Number> salesData = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : getCategorySalesData().entrySet()) {
            salesData.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        salesChart.getData().add(salesData);

        // --- Stats Section (Dynamic values can be injected here later) ---
        Label totalItemsLabel = new Label("Total Items: ");
        Label totalEmployeesLabel = new Label("Total Employees: ");
        Label lowStockLabel = new Label("Low Stock Alerts: ");
        Label recentActivityLabel = new Label("Recent Activity: ");

        VBox statsBox = new VBox(10, totalItemsLabel, totalEmployeesLabel, lowStockLabel, recentActivityLabel);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // --- Final Layout ---
        VBox layout = new VBox(30, title, salesChart, statsBox);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(40));
        layout.getStyleClass().add("dashboard");

        return layout;
    }

    // --- Sample Sales Data (To be replaced by real backend values) ---
    private static Map<String, Integer> getCategorySalesData() {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("Electronics", 120);
        data.put("Clothing", 85);
        data.put("Groceries", 60);
        data.put("Stationery", 40);
        return data;
    }
}
