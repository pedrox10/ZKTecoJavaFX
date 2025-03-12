package app;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CustomToast extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Custom Toast");
        primaryStage.show();

        // Mostrar Toast después de 1 segundo
        Platform.runLater(() -> showToast(root, "Success", "You have successfully completed the trip"));
    }

    private void showToast(StackPane root, String title, String message) {
        Popup popup = new Popup();

        // Icono circular
        Circle iconCircle = new Circle(20, Color.web("#6FCF97")); // Color verde
        Label checkLabel = new Label("✔");
        checkLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        StackPane iconStack = new StackPane(iconCircle, checkLabel);

        // Texto del Toast
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        VBox textContainer = new VBox(titleLabel, messageLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setSpacing(5);

        // Contenedor principal del Toast
        HBox toastContainer = new HBox(iconStack, textContainer);
        toastContainer.setAlignment(Pos.CENTER_LEFT);
        toastContainer.setPadding(new Insets(10));
        toastContainer.setSpacing(15);
        toastContainer.setStyle("-fx-background-color: #E9F7EC; -fx-background-radius: 10; -fx-border-radius: 10;");

        popup.getContent().add(toastContainer);
        popup.setAutoHide(true);
        popup.show(root.getScene().getWindow());

        // Animación de aparición y desaparición
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), toastContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        new Thread(() -> {
            try {
                Thread.sleep(2000); // Duración del Toast
            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), toastContainer);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> popup.hide());
                fadeOut.play();
            });
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
