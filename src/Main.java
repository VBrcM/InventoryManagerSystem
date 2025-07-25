import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import Pages.AccessPage;
import Pages.Layouts.AccessLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the Sales Management System application.
 * Initializes and displays the main application window.
 */
public class Main extends Application {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Starts the JavaFX application and initializes the login view.
     */
    @Override
    public void start(Stage stage) {
        try {
            AccessPage.root = new StackPane(); // Root container for view switching
            Scene scene = new Scene(AccessPage.root, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/Style/Style.css").toExternalForm());

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Sales Management System");
            stage.show();

            AccessLayout.show(); // Show login layout
            logger.info("Application started successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start application", e);
        }
    }

    /**
     * Launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(); // Launch JavaFX lifecycle
    }
}