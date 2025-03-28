package app;

import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import models.Respaldo;
import models.Terminal;
import org.orman.dbms.Database;
import org.orman.dbms.sqlite.SQLite;
import org.orman.mapper.MappingSession;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Database db = new SQLite("horarios.db");
        MappingSession.registerDatabase(db);
        MappingSession.registerEntity(Terminal.class);
        MappingSession.registerEntity(Respaldo.class);
        MappingSession.start();
        Font.loadFont(getClass().getResourceAsStream("/fonts/MaterialIcons-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Medium.ttf"), 12);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
        StackPane root = loader.load();
        // Obtener el controlador y pasarle la referencia del root
        MainController controller = loader.getController();
        controller.setRoot(root);
        Scene scene = new Scene(root, 950, 750);
        scene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
        primaryStage.setTitle("JavaFX ZKTeco");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
