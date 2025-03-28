package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TwoForms extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane stackPane = new StackPane();
// Paso 1: Selección de Backup
        VBox step1 = new VBox();
        ListView<String> lvBackups = new ListView<>();
        lvBackups.getItems().addAll("backup_01.json", "backup_02.json", "backup_03.json");
        Button btnContinuar = new Button("Siguiente");
        step1.getChildren().addAll(lvBackups, btnContinuar);
// Paso 2: Comparación de Usuarios
        VBox step2 = new VBox();
        HBox userComparison = new HBox();
        ListView<String> lvUsuariosBackup = new ListView<>();
        ListView<String> lvUsuariosActuales = new ListView<>();
        userComparison.getChildren().addAll(lvUsuariosBackup, lvUsuariosActuales);
        Button btnVolver = new Button("Atrás");
        step2.getChildren().addAll(userComparison, btnVolver);
        stackPane.getChildren().addAll(step1, step2);
        step2.setVisible(false); // Inicialmente, solo muestra el paso 1
// Evento para cambiar de paso
        btnContinuar.setOnAction(e -> {
            String seleccionado = lvBackups.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                lvUsuariosBackup.getItems().setAll("Usuario 1", "Usuario 2", "Usuario 3");
                lvUsuariosActuales.getItems().setAll("Usuario A", "Usuario B", "Usuario C");
                step1.setVisible(false);
                step2.setVisible(true);
            }
        });
// Evento para regresar
        btnVolver.setOnAction(e -> {
            step2.setVisible(false);
            step1.setVisible(true);
        });
        Scene scene = new Scene(stackPane, 650, 750);
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
