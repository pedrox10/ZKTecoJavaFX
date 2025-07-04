package controllers;

import app.AppConfig;
import app.Main;
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
import java.util.*;

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
    JSONObject backupJSONObject = null;
    JSONObject terminalJSONObject = null;
    boolean fueSincronizado = false;
    LocalDateTime ultimaSincronizacion;
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
            String respuestaTerminal = requestTerminalPorIp(terminal.ip);
            JSONObject respuestaJSONObject = new JSONObject(respuestaTerminal);
            boolean exito = respuestaJSONObject.getBoolean("exito");
            if (exito) {
                lbl_nombre_backup.setText(terminal.nombre);
                lbl_seg_ip.setText(terminal.ip);
                lbl_fecha_respaldo.setText(formatter.format(convertir(respaldoActual.fecha)));

                terminalJSONObject = respuestaJSONObject.getJSONObject("respuesta");
                JSONArray usuariosJSON = terminalJSONObject.getJSONArray("usuarios");
                List<Usuario> usuariosActuales = new ArrayList<>();
                for (int i = 0; i < usuariosJSON.length(); i++) {
                    JSONObject u = usuariosJSON.getJSONObject(i);
                    int id = u.getInt("ci");
                    String nombre = u.getString("nombre");
                    usuariosActuales.add(new Usuario(id, nombre));
                }
                tv_actuales.getItems().setAll(usuariosActuales);
                System.out.println(usuariosActuales);
                lbl_nombre_actual.setText(terminalJSONObject.getString("nombre"));
                lbl_num_actuales.setText(usuariosActuales.size() + " funcionarios");

                vb_primer_paso.setVisible(false);
                vb_segundo_paso.setVisible(true);
                String basePath = Paths.get("").toAbsolutePath().toString();
                String relativePath = "Backups";
                String fullPath = basePath + File.separator + relativePath + File.separator + respaldoActual.nombre;
                File file = new File(fullPath);
                cargarBackup(file);
                String ultimaSync = terminalJSONObject.optString("ultSincronizacion", null);
                fueSincronizado = ultimaSync != null;
                if (fueSincronizado) {
                    ZonedDateTime zonedUtc = ZonedDateTime.parse(terminalJSONObject.getString("ultSincronizacion"));
                    ZonedDateTime zonedLaPaz = zonedUtc.withZoneSameInstant(ZoneId.of("America/La_Paz"));
                    ultimaSincronizacion = zonedLaPaz.toLocalDateTime();
                    lbl_ult_sinc.setText(formatter.format(ultimaSincronizacion));
                    JSONObject respuesta = new JSONObject(filtrarMarcacionesDesde(backupJSONObject.toString(), ultimaSincronizacion));
                    JSONArray marcacionesNuevas = respuesta.getJSONArray("marcaciones");
                    lbl_nuevas_marcaciones.setText("Se agregarán " + marcacionesNuevas.length() + " nuevas marcaciones");
                } else {
                    lbl_ult_sinc.setText("Nunca");
                    lbl_nuevas_marcaciones.setText("Se agregarán " + backupJSONObject.getInt("total_marcaciones") + " nuevas marcaciones");
                }
            } else {
                ToastController toast = ToastController.createToast("info", "Información", respuestaJSONObject.getString("respuesta"));
                toast.show(mc.root);
            }
        } catch (java.net.ConnectException ce) {
            System.err.println("No se puede conectar con el servidor.");
            mostrarError("No se puede conectar con el servidor.");
        } catch (java.net.SocketTimeoutException ste) {
            System.err.println("Tiempo de espera agotado al intentar conectar con el servidor.");
            mostrarError("Tiempo de espera agotado al intentar conectar con el servidor.");
        } catch (IOException ioe) {
            System.err.println("Error de entrada/salida: " + ioe.getMessage());
            mostrarError("Error de entrada/salida.");
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            mostrarError("¡Error inesperado! Revisa los logs.");
        }
    }

    public void mostrarError(String error) {
        ToastController toast = ToastController.createToast("error", "¡Error!", error);
        toast.show(mc.root);
    }

    @FXML
    public void sincronizar(ActionEvent event) {
        JSONArray backupUsuarios = backupJSONObject.getJSONArray("usuarios");
        if (fueSincronizado) {
            String filtradosJSON = filtrarMarcacionesDesde(backupJSONObject.toString(), ultimaSincronizacion);
            String[] respuesta = requestSincronizar(filtradosJSON, backupUsuarios.toString(), terminalJSONObject.getInt("id"));
            int statusCode = Integer.parseInt(respuesta[0]);
            String responseBody = respuesta[1];
            procesarRespuestaSincronizacion(responseBody, statusCode);
        } else {
            JSONObject infoJSON = new JSONObject();
            infoJSON.put("numero_serie", backupJSONObject.getString("numero_serie"));
            infoJSON.put("modelo", backupJSONObject.getString("modelo"));
            infoJSON.put("hora_terminal", backupJSONObject.getString("hora_terminal"));
            infoJSON.put("total_marcaciones", backupJSONObject.getInt("total_marcaciones"));
            infoJSON.put("marcaciones", backupJSONObject.getJSONArray("marcaciones"));
            String[] respuesta = requestSincronizar(infoJSON.toString(), backupUsuarios.toString(), terminalJSONObject.getInt("id"));
            int statusCode = Integer.parseInt(respuesta[0]);
            String responseBody = respuesta[1];
            procesarRespuestaSincronizacion(responseBody, statusCode);
        }
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
            backupJSONObject = new JSONObject(sb.toString());
            // Obtener el array de usuarios
            JSONArray usuariosArray = backupJSONObject.getJSONArray("usuarios");
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
        String urlString = AppConfig.getUrlServidor() + "/terminal/ip/" + ip;
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
        String modelo = originalObj.getString("modelo");
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
        resultado.put("modelo", modelo);
        resultado.put("hora_terminal", horaTerminal);
        resultado.put("total_marcaciones", totalMarcaciones);
        resultado.put("marcaciones", filtradas);
        return resultado.toString(4); // Pretty print con 4 espacios
    }

    public String[] requestSincronizar(String infoJSON, String usuariosJSON, int terminalId) {
        LocalDateTime fechaNueva = convertir(respaldoActual.fecha);

        if ( ultimaSincronizacion == null || fechaNueva.isAfter(ultimaSincronizacion)) {
            if (ultimaSincronizacion != null) {
                String numeroSerieBackup = backupJSONObject.getString("numero_serie");
                String numeroSerieTerminal = terminalJSONObject.getString("numSerie");

                if (!numeroSerieBackup.equals(numeroSerieTerminal)) {
                    // Mostrar alerta de confirmación
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.getDialogPane().getStylesheets().add(Main.class.getResource("/styles/global.css").toExternalForm());
                    alert.setTitle("Confirmación");
                    alert.setHeaderText("Número de serie diferente");
                    Label label = new Label("¡El número de serie del respaldo no coincide con el de el terminal!\n" +
                            "Esto puede ocurrir si se esta cargando por error los datos de otro terminal\n" +
                            "o si el terminal fue cambiado por fallas tecnicas. ¿Deseas continuar?");
                    label.setWrapText(true);
                    label.setStyle("-fx-padding: 10;");
                    alert.getDialogPane().setContent(label);
                    mc.pane_mascara.toFront();

                    Optional<ButtonType> result = alert.showAndWait();
                    if (!result.isPresent() || result.get() != ButtonType.OK) {
                        JSONObject errorJson = new JSONObject();
                        errorJson.put("mensaje", "Información");
                        errorJson.put("detalle", "Sincronizacion cancelada");
                        return new String[]{"-1", errorJson.toString()};
                    }
                }
            }
            //Código del request
            String urlStr = AppConfig.getUrlServidor() + "/terminal/sincronizar/" + terminalId;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                JSONObject finalPayload = new JSONObject();
                finalPayload.put("info", infoJSON); // JSON como texto
                finalPayload.put("usuarios", usuariosJSON);
                String body = finalPayload.toString();
                try (OutputStream os = conn.getOutputStream();
                     OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                    writer.write(body);
                    writer.flush();
                }
                // Leer respuesta
                int responseCode = conn.getResponseCode();
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
                return new String[]{String.valueOf(responseCode), response.toString()};
            } catch (Exception e) {
                e.printStackTrace();
                JSONObject errorJson = new JSONObject();
                errorJson.put("mensaje", "Excepción en cliente");
                errorJson.put("detalle", e.getMessage());
                return new String[]{"500", errorJson.toString()};
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else {
            JSONObject errorJson = new JSONObject();
            errorJson.put("mensaje", "No se puede sincronizar");
            errorJson.put("detalle", "El Terminal ya fué sincronizado con\nuna fecha posterior a este respaldo");
            return new String[]{"-2", errorJson.toString()};
        }
    }

    public void procesarRespuestaSincronizacion(String responseJson, int statusCode) {
        try {
            JSONObject respuesta = new JSONObject(responseJson);
            ToastController toast;
            switch (statusCode) {
                case 200:
                    int nuevasMarcaciones = respuesta.optInt("nuevas_marcaciones", 0);
                    int usuariosAgregados = respuesta.optInt("usuarios_agregados", 0);
                    int usuariosEditados = respuesta.optInt("usuarios_editados", 0);
                    int usuariosEliminados = respuesta.optInt("usuarios_eliminados", 0);
                    String horaServidor = respuesta.getString("hora_servidor");
                    ZonedDateTime zdt = ZonedDateTime.parse(horaServidor);
                    Date fechaSincronizacion = Date.from(zdt.withZoneSameInstant(ZoneId.systemDefault()).toInstant());
                    respaldoActual.fechaSincronizacion = fechaSincronizacion;
                    respaldoActual.update();
                    String mensaje = respuesta.optString("mensaje", "Sincronización exitosa");
                    String resumen = mensaje + "\n\n" +
                            "Nuevas marcaciones: " + nuevasMarcaciones + "\n" +
                            "Usuarios agregados: " + usuariosAgregados + "\n" +
                            "Usuarios editados: " + usuariosEditados + "\n" +
                            "Usuarios eliminados: " + usuariosEliminados;
                    cerrarDialog(null);
                    toast = ToastController.createToast("success", "Sincronización", resumen);
                    toast.show(mc.root);
                    break;
                case 500:
                    toast = ToastController.createToast("error", respuesta.optString("mensaje", "¡Error!"), respuesta.optString("detalle", "Sin detalles"));
                    toast.show(mc.root);
                    break;
                default:
                    toast = ToastController.createToast("info", respuesta.optString("mensaje", "¡Error!"), respuesta.optString("detalle", "Sin detalles"));
                    toast.show(mc.root);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastController toast = ToastController.createToast("error", "Respuesta inválida", e.getMessage());
            toast.show(mc.root);
        }
    }

    public int indiceDe(Respaldo respaldo) {
        int indice = -1;
        for (Respaldo u : respaldos) {
            if (u.id == respaldo.id) {
                indice = respaldos.indexOf(u);
                break;
            }
        }
        return indice;
    }

    public void actualizarRespaldo(Respaldo respaldo) {
        respaldos.set(indiceDe(respaldo), respaldo);
    }
}
