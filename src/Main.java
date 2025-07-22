import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import Pages.AccessPage;
import Pages.Layouts.AccessLayout;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        AccessPage.root = new StackPane();
        Scene scene = new Scene(AccessPage.root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/Style/Style.css").toExternalForm());

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setTitle("Inventory Manager");
        stage.show();

        AccessLayout.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
