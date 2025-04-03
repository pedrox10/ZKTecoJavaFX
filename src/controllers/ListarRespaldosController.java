package controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Respaldo;
import models.Terminal;
import models.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class ListarRespaldosController implements Initializable {

    @FXML
    public VBox vb_primer_paso;
    public VBox vb_segundo_paso;
    public TableView tv_respaldos;
    public TableColumn tc_fecha;
    public TableColumn tc_nombre;
    public TableColumn tc_fecha_sincronizacion;
    public Label ic_fecha;
    public Label ic_archivo;
    public Label ic_sincronizado;
    public Button btn_siguiente;

    public TableView tv_nuevos;
    public TableColumn tc_nuevos_nombre;
    public TableColumn tc_nuevos_ci;
    public TableView tv_actuales;
    public TableColumn tc_actuales_nombre;
    public TableColumn tc_actuales_ci;
    public Label ic_sync;
    public Label ic_warning;

    ObservableList<Respaldo> respaldos = FXCollections.observableArrayList();
    ObjectProperty<Respaldo> opRespaldo = new SimpleObjectProperty<>();
    Terminal terminal = null;
    MainController mc = null;
    Respaldo respaldoActual = null;

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        tc_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tc_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tc_fecha.setCellFactory(column -> new TableCell<Respaldo, Date>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Convertir Date a LocalDateTime
                    LocalDateTime localDateTime = Instant.ofEpochMilli(item.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    // Formatear y mostrar la fecha
                    setText(localDateTime.format(formatter));
                }
            }
        });
        tc_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_fecha_sincronizacion.setCellValueFactory(new PropertyValueFactory<>("fechaSincronizacion"));
        tc_fecha_sincronizacion.setCellFactory(column -> new TableCell<Respaldo, Date>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    LocalDateTime localDateTime = Instant.ofEpochMilli(item.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    setText("El " + localDateTime.format(formatter));
                }
            }
        });
        tv_respaldos.setRowFactory(tv -> new TableRow<Respaldo>() {
            @Override
            protected void updateItem(Respaldo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setDisable(false);
                    setStyle(""); // Restablece el estilo
                } else {
                    if (item.fechaSincronizacion != null) {
                        setDisable(true);
                        setStyle("-fx-opacity: 0.65;"); // Aplica un estilo visual para indicar que está deshabilitado
                    } else {
                        setDisable(false);
                        setStyle("-fx-opacity: 1;"); // Restablece si no está sincronizado
                    }
                }
            }
        });
        tv_respaldos.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Respaldo>() {
            @Override
            public void changed(ObservableValue<? extends Respaldo> observable, Respaldo oldValue, Respaldo newValue) {
                if(newValue != null) {
                    opRespaldo.setValue(newValue);
                }
            }
        });
        opRespaldo.addListener(new ChangeListener<Respaldo>() {
            @Override
            public void changed(ObservableValue<? extends Respaldo> observableValue, Respaldo oldValue, Respaldo newValue) {
                respaldoActual = newValue;
            }
        });
        ic_fecha.setText("\ue916");
        ic_fecha.setStyle("-fx-text-fill: #2fa58f");
        ic_archivo.setText("\ue24d");
        ic_archivo.setStyle("-fx-text-fill: #a295c3");
        ic_sincronizado.setText("\ue915");
        ic_sincronizado.setStyle("-fx-text-fill: #ec3939");
        btn_siguiente.disableProperty().bind(opRespaldo.isNull());

        tc_nuevos_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_nuevos_ci.setCellValueFactory(new PropertyValueFactory<>("ci"));
        tc_actuales_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_actuales_ci.setCellValueFactory(new PropertyValueFactory<>("ci"));
        ic_sync.setText("\ue915");
        ic_sync.setStyle("-fx-text-fill: #ec3939");
        ic_warning.setText("\ue88e");
        ic_warning.setStyle("-fx-font-size: 30px");
    }

    public void initData(Terminal terminal, MainController mc) {
        this.terminal = terminal;
        this.mc = mc;
        respaldos = FXCollections.observableArrayList(Model.fetchQuery(ModelQuery.select().
                from(Respaldo.class).where(C.eq("terminal", terminal.id)).
                getQuery(), Respaldo.class));
        tv_respaldos.setItems(respaldos);
    }

    @FXML
    public void cerrarDialog(ActionEvent event) {
        Stage stage = (Stage) tv_respaldos.getScene().getWindow(); // Obtiene la ventana del diálogo
        stage.close();
        mc.pane_mascara.toBack();
        mc.pane_mascara.setVisible(false);
    }

    @FXML
    public void irSiguiente(ActionEvent event) {
        vb_primer_paso.setVisible(false);
        vb_segundo_paso.setVisible(true);
        File file = new File(respaldoActual.nombre);
        cargarBackup(file);
        try {
            Task<JSONArray> task = getUsuariosTask(1);
            new Thread(task).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void irAtras(ActionEvent event) {
        vb_primer_paso.setVisible(true);
        vb_segundo_paso.setVisible(false);
    }

    public void cargarBackup(File archivoBackup) {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoBackup))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea);
            }
            // Convertir a JSONObject
            JSONObject jsonObject = new JSONObject(sb.toString());
            // Obtener el array de usuarios
            JSONArray usuariosArray = jsonObject.getJSONArray("usuarios");
            // Convertir cada objeto a la clase Usuario
            List<Usuario> nuevosUsuarios = new ArrayList<>();
            for (int i = 0; i < usuariosArray.length(); i++) {
                JSONObject u = usuariosArray.getJSONObject(i);
                int id = u.getInt("user_id");
                String nombre = u.getString("name");
                System.out.println(id + " " + nombre);
                nuevosUsuarios.add(new Usuario(id, nombre));
            }
            tv_nuevos.getItems().setAll(nuevosUsuarios);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método que crea un Task para ejecutar la solicitud sin bloquear la UI
    public Task<JSONArray> getUsuariosTask(int terminalId) {
        return new Task<JSONArray>() {
            @Override
            protected JSONArray call() throws Exception {
                String jsonResponse = requestUsuarios(terminalId);
                return new JSONArray(jsonResponse);
            }
            @Override
            protected void succeeded() {
                super.succeeded();
                JSONArray usuarios = getValue();
                List<Usuario> usuariosActuales = new ArrayList<>();
                for (int i = 0; i < usuarios.length(); i++) {
                    JSONObject u = usuarios.getJSONObject(i);
                    int id = u.getInt("ci");
                    String nombre = u.getString("nombre");
                    usuariosActuales.add(new Usuario(id, nombre));
                }
                tv_actuales.getItems().setAll(usuariosActuales);
            }
            @Override
            protected void failed() {
                super.failed();
                Throwable error = getException();
                error.printStackTrace();
            }
        };
    }

    public static String requestUsuarios(int terminalId) throws Exception {
        String urlString = "http://localhost:4000/api/terminal/" + terminalId + "/usuarios";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Error HTTP: " + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        conn.disconnect();
        return response.toString();
    }
}
