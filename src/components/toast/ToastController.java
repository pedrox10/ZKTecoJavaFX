package components.toast;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.io.IOException;

public class ToastController {
    @FXML private HBox toastContainer;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Label iconLabel;

    private Popup popup;

    public static ToastController createToast(String tipo, String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(ToastController.class.getResource("Toast.fxml"));
            HBox toast = loader.load();
            ToastController controller = loader.getController();
            controller.setup(tipo, title, message);
            return controller;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setup(String tipo, String title, String message) {
        switch (tipo) {
            case "success":
                iconLabel.setStyle("-fx-text-fill: #2f902f;");
                iconLabel.setText("\ue86c");
                break;
            case "error":
                iconLabel.setStyle("-fx-text-fill: #ce5656;");
                iconLabel.setText("\ue15c");
                break;
            case "info":
                iconLabel.setStyle("-fx-text-fill: #f8c453;");
                iconLabel.setText("\ue88e");
        }

        titleLabel.setText(title);
        messageLabel.setText(message);
        popup = new Popup();
        popup.getContent().add(toastContainer);
        popup.setAutoHide(true);
    }

    public void show(StackPane root) {
        Platform.runLater(() -> {
            popup.show(root.getScene().getWindow());

            // Centrar en la parte inferior
            double centerX = root.getScene().getWidth() / 2 - toastContainer.getWidth() / 2;
            double bottomY = root.getScene().getHeight() - 80;
            popup.setX(root.localToScreen(centerX, 0).getX());
            popup.setY(root.localToScreen(0, bottomY).getY());

            // Animación de aparición
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), toastContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Auto cerrar después de 3 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {}

                Platform.runLater(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), toastContainer);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(e -> popup.hide());
                    fadeOut.play();
                });
            }).start();
        });
    }
}