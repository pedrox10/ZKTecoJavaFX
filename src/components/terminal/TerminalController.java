package components.terminal;

import app.Main;
import components.toast.ToastController;
import controllers.AgregarTerminalController;
import controllers.ListarRespaldosController;
import controllers.MainController;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import models.Respaldo;
import models.Terminal;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class TerminalController implements Initializable {

    @FXML
    private VBox vb_cerrar;
    @FXML
    private Label lbl_editar;
    @FXML
    private VBox vb_cabecera;
    @FXML
    private Label lbl_nombre;
    @FXML
    private Label lbl_ip;
    @FXML
    private Label lbl_puerto;
    @FXML
    private Label lbl_ult_sinc;
    @FXML
    private Button btn_respaldo;
    @FXML
    private Label lbl_upload;
    ObjectProperty<StackPane> op_root = new SimpleObjectProperty<StackPane>();
    StackPane root;
    Terminal terminal = null;
    ObjectProperty<Terminal> op_terminal = new SimpleObjectProperty();
    MainController mc;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vb_cabecera.getStylesheets().add(getClass().getResource("Terminal.css").toExternalForm());
        lbl_nombre.getStyleClass().add("terminal-titulo");
        lbl_ip.getStyleClass().add("subtitulo");
        lbl_editar.setText("\ue3c9");
        //lbl_editar.setStyle("-fx-font-size: 18px");
        vb_cerrar.toFront();
        lbl_upload.setText("\ue2c3");
        vb_cabecera.getStyleClass().add("terminal");
        lbl_ult_sinc.getStyleClass().add("terminal-sync");

        op_root.addListener(new ChangeListener<StackPane>() {
            @Override
            public void changed(ObservableValue<? extends StackPane> observable, StackPane oldValue, StackPane newValue) {
                if (newValue != null) {
                    root = newValue;
                    System.out.println(root.getStyle());
                }
            }
        });

        op_terminal.addListener(new ChangeListener<Terminal>() {
            @Override
            public void changed(ObservableValue<? extends Terminal> observableValue, Terminal newValue, Terminal oldValue) {
                terminal = newValue;
            }
        });
    }

    // Método para actualizar los datos de la tarjeta
    public void setTerminalData(Terminal terminal, StackPane root, MainController mc) {
        this.mc = mc;
        this.terminal = terminal;
        lbl_nombre.setText(terminal.getNombre());
        lbl_ip.setText(terminal.getIp());
        if (terminal.getUltimoRespaldo() == null) {
            lbl_ult_sinc.setText("Nunca");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY hh:mm");
            lbl_ult_sinc.setText(sdf.format(terminal.getUltimoRespaldo().fecha));
        }
        op_root.setValue(root);
    }

    @FXML
    private void editarTerminal() throws IOException{
        Dialog dialogo = new Dialog();
        dialogo.setTitle("Editar Terminal");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AgregarTerminal.fxml"));
        Parent terminalNode = loader.load();
        //Obtener el controlador y pasarle los datos
        AgregarTerminalController atc = loader.getController();
        atc.initData(terminal);
        AnchorPane root = (AnchorPane) loader.getRoot();
        dialogo.getDialogPane().getStylesheets().add(Main.class.getResource("/styles/global.css").toExternalForm());
        dialogo.getDialogPane().setContent(root);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ButtonBar buttonBar = (ButtonBar) dialogo.getDialogPane().lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_LINUX); // "Cancelar" antes que "Aceptar"
        }
        dialogo.show();
        mc.pane_mascara.setVisible(true);
        mc.pane_mascara.toFront();

        Button btn_ok = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        btn_ok.addEventFilter(ActionEvent.ACTION, (ae) -> {
            String nombre = atc.tf_nombre.getText().trim();
            String ip = atc.tf_ip.getText().trim();
            String puerto = atc.tf_puerto.getText().trim();
            String respuesta = mc.validarDatos(nombre, ip, puerto);
            if( respuesta != "Correcto") {
                ae.consume();
                atc.hb_mensaje.setVisible(true);
                atc.lbl_mensaje.setText(respuesta);
            } else {
                terminal.nombre = nombre;
                terminal.ip = ip;
                terminal.puerto = Integer.parseInt(puerto);
                terminal.update();
                mc.editarTerminalUI(terminal);
            }
        });
        dialogo.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                mc.pane_mascara.toBack();
                mc.pane_mascara.setVisible(false);
            }
        });
    }

    @FXML
    public void eliminarTerminal() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.class.getResource("/styles/global.css").toExternalForm());
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar");
        alert.setContentText("¿Estás seguro de que deseas eliminar este terminal?");
        Label contenido = new Label("¿Estás seguro de que deseas eliminar este terminal?");
        contenido.setStyle("-fx-font-size: 14px; -fx-padding: 25 15 25 15;");
        alert.getDialogPane().setContent(contenido);
        mc.pane_mascara.setVisible(true);
        mc.pane_mascara.toFront();
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            mc.eliminarTerminalUI(terminal);
            mc.pane_mascara.toBack();
            mc.pane_mascara.setVisible(false);
        } else {
            mc.pane_mascara.toBack();
            mc.pane_mascara.setVisible(false);
        }
    }

    @FXML
    private void verificarConexion() throws IOException {
        String respuesta = ejecutarScriptPython("scriptpy/conectar.py", this.terminal.ip, this.terminal.puerto + " ");
        System.out.println(respuesta);
        JSONObject respuestaJson = new JSONObject(respuesta);
        boolean conectado = respuestaJson.getBoolean("connected");
        ToastController toast;
        if (conectado) {
            toast = ToastController.createToast("success", "Listo", "Terminal con conexión");
            toast.show(root);
            btn_respaldo.setDisable(false);
        } else {
            toast = ToastController.createToast("error", "Error", "No se pudo conectar");
            toast.show(root);
        }
    }

    @FXML
    private void generarBackup() throws IOException {
        iniciarCarga(root);
    }

    @FXML
    private void verRespaldos() throws IOException{
        Dialog dialogo = new Dialog();
        dialogo.setTitle("Copias de Respaldo");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ListarRespaldos.fxml"));
        loader.load();
        //Obtener el controlador y pasarle los datos
        ListarRespaldosController lrc = loader.getController();
        lrc.initData(terminal, mc);
        AnchorPane root = (AnchorPane) loader.getRoot();
        dialogo.getDialogPane().getStylesheets().add(Main.class.getResource("/styles/global.css").toExternalForm());
        dialogo.getDialogPane().setContent(root);
        Node buttonBar = dialogo.getDialogPane().lookup(".button-bar");
        if (buttonBar != null) {
            ((Pane) buttonBar.getParent()).getChildren().remove(buttonBar);
        }
        Window window = dialogo.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                dialogo.close();
                mc.pane_mascara.toBack();
                mc.pane_mascara.setVisible(false);
            }
        });
        dialogo.show();
        mc.pane_mascara.toFront();
        mc.pane_mascara.setVisible(true);
    }

    private void iniciarCarga(StackPane root) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        Label textoEspera = new Label("Generando copia de respaldo ...");
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
                    Date fechaActual = new Date();
                    String nombreArchivo = "Backup_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(fechaActual) + ".json";
                    String basePath = Paths.get("").toAbsolutePath().toString();
                    String relativePath = "Backups";
                    String fullPath = basePath + File.separator + relativePath + File.separator + nombreArchivo;
                    File backupFolder = new File(basePath, relativePath);
                    if (!backupFolder.exists()) {
                        backupFolder.mkdirs();
                    }
                    try (FileWriter file = new FileWriter(fullPath)) {
                        file.write(jsonObject.toString(4)); // JSON con indentación
                        System.out.println("Archivo JSON generado en: " + fullPath);
                        Respaldo respaldo = new Respaldo();
                        respaldo.fecha = fechaActual;
                        respaldo.nombre = nombreArchivo;  // Guardar solo el nombre del archivo
                        respaldo.terminal = terminal;
                        Platform.runLater(() -> respaldo.insert());
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

