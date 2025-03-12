package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Popup;

public class LoadingExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btnStart = new Button("Iniciar Proceso");
        StackPane root = new StackPane(btnStart);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cargando en JavaFX");
        primaryStage.show();

        btnStart.setOnAction(e -> startLoading(root));
    }

    private void startLoading(StackPane root) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(125, 125); // FORZAR tamaño pequeño
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Fondo semi-transparente
        loadingPane.setAlignment(Pos.CENTER);

        root.getChildren().add(loadingPane);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws InterruptedException {
                Thread.sleep(3000); // Simulación de proceso
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    root.getChildren().remove(loadingPane);
                    showToast(root, "Proceso completado!");
                });
            }
        };

        new Thread(task).start();
    }

    private void showToast(StackPane root, String message) {
        Popup popup = new Popup();
        Button toast = new Button(message);
        toast.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 10;");
        toast.setDisable(true);

        popup.getContent().add(toast);
        popup.setAutoHide(true);
        popup.show(root.getScene().getWindow());

        new Thread(() -> {
            try {
                Thread.sleep(2000); // Duración del toast
            } catch (InterruptedException ignored) {}
            Platform.runLater(popup::hide);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
