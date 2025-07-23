package Pages;

import Pages.Layouts.AccessLayout;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AccessPage extends Application {

    public static StackPane root;

    @Override
    public void start(Stage stage) {
        root = new StackPane();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/Style/Style.css").toExternalForm());

        Rectangle2D screen = Screen.getPrimary().getBounds();
        stage.setX(screen.getMinX());
        stage.setY(screen.getMinY());
        stage.setWidth(screen.getWidth());
        stage.setHeight(screen.getHeight());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setResizable(false);
        stage.show();

        AccessLayout.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}