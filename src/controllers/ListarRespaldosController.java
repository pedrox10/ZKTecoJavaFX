package controllers;

import components.toast.ToastController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    public Label lbl_pri_nombre;
    public Label lbl_pri_ip;
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
    public Label lbl_nombre_backup;
    public Label lbl_nombre_actual;
    public Label lbl_seg_ip;
    public Label lbl_fecha_respaldo;
    public Label lbl_ult_sinc;
    public Label lbl_num_nuevos;
    public Label lbl_num_actuales;
    public Label lbl_nuevas_marcaciones;
    public Label ic_sync;
    public Label ic_warning;
    public CheckBox cb_confirmacion;
    public Button btn_sincronizar;

    ObservableList<Respaldo> respaldos = FXCollections.observableArrayList();
    ObjectProperty<Respaldo> opRespaldo = new SimpleObjectProperty<>();
    Terminal terminal = null;
    MainController mc = null;
    Respaldo respaldoActual = null;
    JSONObject jsonBackup = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        tc_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tc_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tc_fecha.setCellFactory(column -> new TableCell<Respaldo, Date>() {
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
                if (newValue != null) {
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
        btn_sincronizar.disableProperty().bind(cb_confirmacion.selectedProperty().not());
        //ic_warning.setStyle("-fx-font-size: 30px");
    }

    public void initData(Terminal terminal, MainController mc) {
        this.terminal = terminal;
        this.mc = mc;
        respaldos = FXCollections.observableArrayList(Model.fetchQuery(ModelQuery.select().
                from(Respaldo.class).where(C.eq("terminal", terminal.id)).orderBy("-Respaldo.fecha").
                getQuery(), Respaldo.class));
        tv_respaldos.setItems(respaldos);
        lbl_pri_nombre.setText(terminal.nombre);
        lbl_pri_ip.setText(terminal.ip);
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
        try {
            String res = requestTerminalPorIp(terminal.ip);
            JSONObject jsonObject = new JSONObject(res);
            boolean exito = jsonObject.getBoolean("exito");
            if (exito) {
                lbl_nombre_backup.setText(terminal.nombre);
                lbl_seg_ip.setText(terminal.ip);
                lbl_fecha_respaldo.setText(formatter.format(convertir(respaldoActual.fecha)));

                JSONObject resObj = jsonObject.getJSONObject("respuesta");
                ZonedDateTime zonedUtc = ZonedDateTime.parse(resObj.getString("ultSincronizacion"));
                ZonedDateTime zonedLaPaz = zonedUtc.withZoneSameInstant(ZoneId.of("America/La_Paz"));
                LocalDateTime ult_sinc = zonedLaPaz.toLocalDateTime();

                JSONArray usuariosJSON = resObj.getJSONArray("usuarios");
                List<Usuario> usuariosActuales = new ArrayList<>();
                for (int i = 0; i < usuariosJSON.length(); i++) {
                    JSONObject u = usuariosJSON.getJSONObject(i);
                    int id = u.getInt("ci");
                    String nombre = u.getString("nombre");
                    usuariosActuales.add(new Usuario(id, nombre));
                }
                tv_actuales.getItems().setAll(usuariosActuales);
                lbl_nombre_actual.setText(resObj.getString("nombre"));
                lbl_num_actuales.setText(usuariosActuales.size() + " funcionarios");
                lbl_ult_sinc.setText(formatter.format(ult_sinc));
                vb_primer_paso.setVisible(false);
                vb_segundo_paso.setVisible(true);
                String basePath = Paths.get("").toAbsolutePath().toString();
                String relativePath = "Backups";
                String fullPath = basePath + File.separator + relativePath + File.separator + respaldoActual.nombre;
                File file = new File(fullPath);
                cargarBackup(file);
                String postJSON = filtrarMarcacionesDesde(jsonBackup.toString(), ult_sinc);
                System.out.println(requestSincronizar(postJSON, usuariosJSON.toString(), 1));
            } else {
                mostrarError(jsonObject.getString("respuesta"));
            }
        } catch (java.net.ConnectException ce) {
            System.err.println("No se pudo conectar con el servidor");
            mostrarError("No se pudo conectar con el servidor");
        } catch (java.net.SocketTimeoutException ste) {
            System.err.println("Tiempo de espera agotado al intentar conectar con el servidor.");
            mostrarError("No se pudo conectar con el servidor");
        } catch (IOException ioe) {
            System.err.println("Error de entrada/salida: " + ioe.getMessage());
            mostrarError("Error I/O");
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            mostrarError("¡Error inesperado!");
        }
    }

    public void mostrarError(String error) {
        ToastController toast = ToastController.createToast("error", "¡Error!", error);
        toast.show(mc.root);
    }

    @FXML
    public void irAtras(ActionEvent event) {
        cb_confirmacion.setSelected(false);
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
            jsonBackup = new JSONObject(sb.toString());
            // Obtener el array de usuarios
            JSONArray usuariosArray = jsonBackup.getJSONArray("usuarios");
            // Convertir cada objeto a la clase Usuario
            List<Usuario> nuevosUsuarios = new ArrayList<>();
            for (int i = 0; i < usuariosArray.length(); i++) {
                JSONObject u = usuariosArray.getJSONObject(i);
                int id = u.getInt("user_id");
                String nombre = u.getString("name");
                nuevosUsuarios.add(new Usuario(id, nombre));
            }
            tv_nuevos.getItems().setAll(nuevosUsuarios);
            lbl_num_nuevos.setText(nuevosUsuarios.size() + " funcionarios");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String requestTerminalPorIp(String ip) throws IOException {
        String urlString = "http://localhost:4000/api/terminal/ip/" + ip;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        conn.disconnect();
        String jsonResponse = response.toString();
        return jsonResponse;
    }

    public LocalDateTime convertir(Date fecha) {
        LocalDateTime localDateTime = fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime;
    }

    public String filtrarMarcacionesDesde(String jsonOriginal, LocalDateTime fechaSync) {
        JSONObject originalObj = new JSONObject(jsonOriginal);
        JSONArray marcaciones = originalObj.getJSONArray("marcaciones");
        String numeroSerie = originalObj.getString("numero_serie");
        int totalMarcaciones = originalObj.getInt("total_marcaciones");
        String horaTerminal = originalObj.getString("hora_terminal");
        // Filtrar marcaciones
        JSONArray filtradas = new JSONArray();
        for (int i = 0; i < marcaciones.length(); i++) {
            JSONObject m = marcaciones.getJSONObject(i);
            LocalDateTime ts = LocalDateTime.parse(m.getString("timestamp"));
            if (!ts.isBefore(fechaSync)) {
                filtradas.put(m);
            }
        }
        // Crear resultado
        JSONObject resultado = new JSONObject();
        resultado.put("numero_serie", numeroSerie);
        resultado.put("hora_terminal", horaTerminal);
        resultado.put("total_marcaciones", totalMarcaciones);
        resultado.put("marcaciones", filtradas);

        return resultado.toString(4); // Pretty print con 4 espacios
    }

    public String requestSincronizar(String infoJSON, String usuariosJSON, int terminalId) {
        String urlStr = "http://localhost:4000/api/terminal/sincronizar/" + terminalId;
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            // Crear el JSON combinado como texto
            JSONObject finalPayload = new JSONObject();
            finalPayload.put("info", infoJSON); // JSON como texto
            finalPayload.put("usuarios", usuariosJSON);       // JSON como texto
            String body = finalPayload.toString();
            try (OutputStream os = conn.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(body);
                writer.flush();
            }
            // Leer respuesta
            int responseCode = conn.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
