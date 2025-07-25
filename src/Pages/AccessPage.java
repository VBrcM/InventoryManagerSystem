package Pages;

import Pages.Layouts.AccessLayout;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.logging.Logger;

public class AccessPage extends Application {

    public static StackPane root;

    private static final Logger logger = Logger.getLogger(AccessPage.class.getName());

    /**
     * Entry point of the JavaFX application. Initializes the main stage, configures the layout, and shows the access screen.
     */
    @Override
    public void start(Stage stage) {
        // Root container for scene content
        root = new StackPane();

        // Load stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/Style/Style.css").toExternalForm());

        // Set stage to fill the entire screen
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

        logger.info("Application started and stage initialized");

        // Show access layout
        AccessLayout.show();
    }

    /**
     * Launches the JavaFX application.
     */
    public static void main(String[] args) {
        logger.info("Launching AccessPage application");
        launch(args);
    }
}