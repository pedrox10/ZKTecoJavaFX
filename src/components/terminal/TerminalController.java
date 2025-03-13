package components.terminal;

import components.toast.ToastController;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Terminal;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class TerminalController implements Initializable {

    @FXML
    private HBox hbCerrar;
    @FXML
    private Label lblCerrar;
    @FXML
    private VBox terminalPane;
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblIP;
    @FXML
    private Label lblPuerto;
    @FXML
    private Label lblUltSincronizacion;
    @FXML
    private Button btnRespaldo;
    @FXML
    private Label lblUpload;
    ObjectProperty<StackPane> op_root = new SimpleObjectProperty<StackPane>();
    StackPane root;
    Terminal terminal;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        terminalPane.getStylesheets().add(getClass().getResource("Terminal.css").toExternalForm());
        lblNombre.getStyleClass().add("terminal-titulo");
        lblIP.getStyleClass().add("subtitulo");
        lblCerrar.setText("\ue5cd");
        lblCerrar.setStyle("-fx-pref-width: 20; -fx-text-fill: #e67474");
        hbCerrar.toFront();
        lblUpload.setText("\ue2c3");
        terminalPane.getStyleClass().add("terminal");
        lblUltSincronizacion.getStyleClass().add("terminal-sync");

        op_root.addListener(new ChangeListener<StackPane>() {
            @Override
            public void changed(ObservableValue<? extends StackPane> observable, StackPane oldValue, StackPane newValue) {
                if (newValue != null) {
                    root = newValue;
                    System.out.println(root.getStyle());
                }
            }
        });
    }

    // Método para actualizar los datos de la tarjeta
    public void setTerminalData(Terminal terminal, StackPane root) {
        this.terminal = terminal;
        lblNombre.setText(terminal.getNombre());
        lblIP.setText(terminal.getIp());
        if (terminal.getUltimoRespaldo() == null) {
            lblUltSincronizacion.setText("Nunca");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY hh:mm");
            lblUltSincronizacion.setText(sdf.format(terminal.getUltimoRespaldo().fecha));
        }
        op_root.setValue(root);
    }

    @FXML
    private void verificarConexion() throws IOException {
        String respuesta = ejecutarScriptPython("scriptpy/conectar.py", this.terminal.ip, this.terminal.puerto + " ");
        JSONObject respuestaJson = new JSONObject(respuesta);
        boolean conectado = respuestaJson.getBoolean("connected");
        ToastController toast;
        if (conectado) {
            toast = ToastController.createToast("success", "Listo", "Terminal con conexión");
            toast.show(root);
            btnRespaldo.setDisable(false);
        } else {
            toast = ToastController.createToast("error", "Error", "No se pudo conectar");
            toast.show(root);
        }
    }

    @FXML
    private void generarBackup() throws IOException {
        startLoading(root);
    }

    private void startLoading(StackPane root) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        Label textoEspera = new Label("Generando respaldo ...");
        textoEspera.getStyleClass().add("texto-espera");
        VBox loadingPane = new VBox(progressIndicator, textoEspera);
        loadingPane.setSpacing(10);
        loadingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Fondo semi-transparente
        loadingPane.setAlignment(Pos.CENTER);
        root.getChildren().add(loadingPane);
        final String ip = this.terminal.ip;
        final String puerto = this.terminal.puerto + " ";

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws InterruptedException {
                try {
                    String usuariosJson = ejecutarScriptPython("scriptpy/usuarios.py", ip, puerto + " ");
                    String marcacionesJson = ejecutarScriptPython("scriptpy/marcaciones.py", ip, puerto + " ");
                    // Procesar los datos obtenidos de los scripts
                    JSONArray usuariosArray = new JSONArray(usuariosJson);
                    JSONObject respuestaJson = new JSONObject(marcacionesJson);
                    JSONArray marcacionesArray = respuestaJson.getJSONArray("marcaciones");
                    // Crear el objeto JSON final con ambos
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("usuarios", usuariosArray);
                    jsonObject.put("marcaciones", marcacionesArray);
                    jsonObject.put("numero_serie", respuestaJson.getString("numero_serie"));
                    jsonObject.put("hora_terminal", respuestaJson.getString("hora_terminal"));
                    jsonObject.put("total_marcaciones", respuestaJson.getInt("total_marcaciones"));
                    // Generar el archivo JSON con la fecha actual
                    String fileName = "backup_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json";
                    try (FileWriter file = new FileWriter(fileName)) {
                        file.write(jsonObject.toString(4)); // El parámetro '4' es para un JSON con indentación legible
                        System.out.println("Archivo JSON generado: " + fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    root.getChildren().remove(loadingPane);
                    ToastController toast = ToastController.createToast("success", "¡Listo!", "Datos guardados correctamente");
                    toast.show(root);
                });
            }
        };
        new Thread(task).start();
    }


    public static String ejecutarScriptPython(String scriptPath, String ip, String puerto) throws IOException {
        // Construir el comando
        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, ip, puerto);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        // Leer la salida del script Python
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }
}

