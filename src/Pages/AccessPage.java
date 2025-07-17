package Pages;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AccessPage extends Application {

    // ===== Root layout container for switching pages =====
    public static StackPane root;

    @Override
    public void start(Stage stage) {
        // ===== Initialize root container =====
        root = new StackPane();

        // ===== Create scene and attach stylesheet =====
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/Style/Style.css").toExternalForm());

        // ===== Set stage size to match full screen bounds =====
        Rectangle2D screen = Screen.getPrimary().getBounds();
        stage.setX(screen.getMinX());
        stage.setY(screen.getMinY());
        stage.setWidth(screen.getWidth());
        stage.setHeight(screen.getHeight());

        // ===== Remove window border/title bar =====
        stage.initStyle(StageStyle.UNDECORATED);

        // ===== Apply scene and enable fullscreen =====
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setResizable(false);

        stage.show();

        // ===== Show initial login layout =====
        AccessLayout.show();
    }

    // ===== Launch JavaFX application =====
    public static void main(String[] args) {
        launch(args);
    }
}
