package Dialogs;

import Model.POJO.SaleItem;
import Pages.AccessPage;
import DB.Formatter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class ReceiptDialog {

    public static void showContent(LocalDateTime time, List<SaleItem> items, int itemCount) {
        StackPane root = AccessPage.root;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());
        overlay.setOnMouseClicked(MouseEvent::consume);

        VBox dialogBox = new VBox(15);
        dialogBox.setPadding(new Insets(30));
        dialogBox.setAlignment(Pos.TOP_CENTER);
        dialogBox.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 12;");
        dialogBox.maxWidthProperty().bind(root.widthProperty().multiply(0.6));
        dialogBox.maxHeightProperty().bind(root.heightProperty().multiply(0.8));

        // === Title ===
        Label title = new Label("TRANSACTION");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // === Date & Time ===
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String dateStr = time.format(formatter);
        String timeStr = time.toLocalTime().withNano(0).toString();

        Label dateLabel = new Label("Date: " + dateStr);
        Label timeLabel = new Label("Time: " + timeStr);
        Stream.of(dateLabel, timeLabel).forEach(l -> l.setStyle("-fx-text-fill: white;"));

        VBox dateTimeBox = new VBox(5, dateLabel, timeLabel);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        // === TableView Setup ===
        TableView<SaleItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setMaxHeight(250);
        table.setStyle("""
            -fx-background-color: #2e2e2e;
            -fx-control-inner-background: #2e2e2e;
            -fx-selection-bar: #444444;
            -fx-selection-bar-non-focused: #444444;
            -fx-text-fill: white;
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
        """);

        // === Define columns
        TableColumn<SaleItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getProductName()));

        TableColumn<SaleItem, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSiPrice()));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatter.formatCurrency(item));
            }
        });

        TableColumn<SaleItem, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getSiQty() * cellData.getValue().getSiPrice();
            return new ReadOnlyObjectWrapper<>(subtotal);
        });
        subtotalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatter.formatCurrency(item));
            }
        });

// === Column alignment (optional)
        Stream.of(nameCol, priceCol, subtotalCol).forEach(col -> col.setStyle("-fx-alignment: CENTER-LEFT;"));

// === Add columns to table
        table.getColumns().addAll(nameCol, priceCol, subtotalCol);
        table.setItems(FXCollections.observableArrayList(items));

// === Set column widths by % of total table width
        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            nameCol.setPrefWidth(width * 0.6);
            priceCol.setPrefWidth(width * 0.2);
            subtotalCol.setPrefWidth(width * 0.2);
        });

        VBox.setVgrow(table, Priority.ALWAYS);

        // === ScrollPane for Table ===
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("""
        -fx-background-color: transparent;
        -fx-border-color: transparent;
        -fx-background-insets: 0;
        -fx-padding: 0;
                    """);

        // === Summary (Product count and total) ===
        double total = items.stream()
                .mapToDouble(i -> i.getSiQty() * i.getSiPrice())
                .sum();

        Label countLabel = new Label("Product Count: " + itemCount);
        countLabel.setStyle("-fx-text-fill: #aaaaaa;");

        Label totalLabel = new Label("Total Amount: ₱" + Formatter.formatCurrency(total));
        totalLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox summaryBox = new VBox(5, countLabel, totalLabel);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(10, 0, 0, 0));

        // === Back Button Centered ===
        Label backButton = new Label("Back");
        backButton.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20;");
        backButton.setOnMouseClicked(e -> root.getChildren().remove(overlay));

        HBox backBox = new HBox(backButton);
        backBox.setAlignment(Pos.CENTER);
        backBox.setPadding(new Insets(10));

        // === Final Assembly ===
        dialogBox.getChildren().addAll(title, dateTimeBox, scrollPane, summaryBox, backBox);
        overlay.getChildren().add(dialogBox);
        StackPane.setAlignment(dialogBox, Pos.CENTER);
        root.getChildren().add(overlay);
    }
}