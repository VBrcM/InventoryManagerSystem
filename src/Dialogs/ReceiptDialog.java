package Dialogs;

import DB.AppFormatter;
import Model.POJO.SaleItem;
import Pages.AccessPage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Displays a receipt dialog showing sale information, including items purchased,
 * quantity, unit price, and total amount. Utilizes CSS-based styling and integrates
 * with DialogManager for modal control.
 */
public class ReceiptDialog {

    private static final Logger logger = Logger.getLogger(ReceiptDialog.class.getName());

    /**
     * Builds and shows the receipt layout with formatted data and table.
     */
    public static void showContent(LocalDateTime time, List<SaleItem> items, int itemCount) {
        VBox dialogBox = new VBox(15);
        dialogBox.getStyleClass().add("receipt-container");
        dialogBox.setAlignment(Pos.TOP_CENTER);
        dialogBox.maxWidthProperty().bind(AccessPage.root.widthProperty().multiply(0.8));
        dialogBox.maxHeightProperty().bind(AccessPage.root.heightProperty().multiply(0.8));

        Label title = new Label("RECEIPT");
        title.getStyleClass().add("receipt-title");
        title.setPadding(new Insets(0, 0, 12, 0));

        String dateStr = AppFormatter.formatDate(time);
        String timeStr = AppFormatter.formatTime(time);

        Label dateLabel = new Label("Date:");
        dateLabel.getStyleClass().add("receipt-message-bold");
        Label dateValue = new Label(dateStr);
        dateValue.getStyleClass().add("receipt-message");

        HBox dateRow = new HBox(5, dateLabel, dateValue);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("Time:");
        timeLabel.getStyleClass().add("receipt-message-bold");
        Label timeValue = new Label(timeStr);
        timeValue.getStyleClass().add("receipt-message");

        HBox timeRow = new HBox(5, timeLabel, timeValue);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        VBox dateTimeBox = new VBox(4, dateRow, timeRow);
        dateTimeBox.setAlignment(Pos.CENTER_RIGHT);

        int saleId = items.isEmpty() ? 0 : items.getFirst().getSaleId();

        Label idLabel = new Label("Sale ID:");
        idLabel.getStyleClass().add("receipt-message-bold");
        Label idValue = new Label(String.valueOf(saleId));
        idValue.getStyleClass().add("receipt-message");

        VBox idBox = new VBox(5, idLabel, idValue);
        idBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(40, idBox, spacer, dateTimeBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 0, 10, 0));

        TableView<SaleItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<SaleItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getProduct().getProductName()));

        TableColumn<SaleItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSiQty()));

        TableColumn<SaleItem, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSiPrice()));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : AppFormatter.formatCurrency(item));
            }
        });

        TableColumn<SaleItem, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(c -> {
            double subtotal = c.getValue().getSiQty() * c.getValue().getSiPrice();
            return new ReadOnlyObjectWrapper<>(subtotal);
        });
        subtotalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : AppFormatter.formatCurrency(item));
            }
        });

        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        qtyCol.setStyle("-fx-alignment: CENTER;");
        priceCol.setStyle("-fx-alignment: CENTER;");
        subtotalCol.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(nameCol, qtyCol, priceCol, subtotalCol);
        table.setItems(FXCollections.observableArrayList(items));

        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            nameCol.setPrefWidth(width * 0.45);
            qtyCol.setPrefWidth(width * 0.15);
            priceCol.setPrefWidth(width * 0.2);
            subtotalCol.setPrefWidth(width * 0.2);
        });

        VBox.setVgrow(table, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        double total = items.stream().mapToDouble(i -> i.getSiQty() * i.getSiPrice()).sum();

        Label countLabel = new Label("Product Count: " + itemCount);
        countLabel.getStyleClass().add("receipt-message");

        Label totalLabel = new Label("Total Amount: " + AppFormatter.formatCurrency(total));
        totalLabel.getStyleClass().add("receipt-message");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox summaryBox = new VBox(5, countLabel, totalLabel);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(10, 0, 0, 0));

        Label backButton = new Label("Back");
        backButton.getStyleClass().add("receipt-button");
        backButton.setOnMouseClicked(e -> DialogManager.closeDialog());

        HBox backBox = new HBox(backButton);
        backBox.setAlignment(Pos.CENTER);
        backBox.setPadding(new Insets(10));

        dialogBox.getChildren().addAll(title, headerRow, scrollPane, summaryBox, backBox);
        DialogManager.showDialog(dialogBox);

        logger.info("Displayed receipt dialog with " + itemCount + " items and total " + totalLabel.getText());
    }
}
