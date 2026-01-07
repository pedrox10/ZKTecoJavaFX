package controllers;

import app.Main;
import components.terminal.TerminalController;
import components.toast.ToastController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class AdmFuncionariosController implements Initializable {

    @FXML
    public Label lbl_titulo;
    public TableView<Funcionario> tv_funcionarios;
    public TableColumn<Funcionario, Boolean> tc_check;
    public TableColumn tc_num;
    public TableColumn tc_nombre;
    public TableColumn tc_ci;
    public VBox vb_confirmar_eliminar;
    public VBox vb_reporte_eliminacion;
    public VBox vb_reporte_restauracion;
    public VBox vb_cargando;
    public Button btn_respaldar;
    public Button btn_cargar;
    public Label icon_eliminar;
    public Label icon_respaldar;
    public Label icon_cargar;
    public TextField tf_busqueda;
    public Label lbl_seleccionados;
    public Label lbl_filtrados;
    public StackPane overlay;
    public CheckBox cb_confirmar_eliminar;
    public Button btn_aceptar_eliminar;
    public ListView<Funcionario> lv_funcionarios;
    public TableView<ReporteEliminacion> tv_reporte;
    public TableColumn tc_num_reporte;
    public TableColumn tc_nombre_reporte;
    public TableColumn tc_mensaje_reporte;
    public TableColumn tc_icono;
    public TableView<ReporteRestauracion> tv_restaurados;
    public TableColumn tc_num_restaurado;
    public TableColumn tc_nombre_restaurado;
    public TableColumn tc_mensaje_restaurado;
    public TableColumn tc_icono_restaurado;
    public VBox vb_funcionarios;
    public VBox vb_cargar_respaldo;
    public Label lbl_respaldados;
    public TableView<FuncionarioRespaldado> tv_respaldados;
    public TableColumn<FuncionarioRespaldado, Boolean> tc_check_respaldado;
    public TableColumn tc_nombre_respaldado;
    public TableColumn tc_ci_respaldado;
    public TableColumn<FuncionarioRespaldado, Integer> tc_num_huellas;
    public TableColumn tc_rol;

    Terminal terminal= null;
    MainController mc = null;
    ObservableList<Funcionario> funcionarios = FXCollections.observableArrayList();
    private FilteredList<Funcionario> funcionariosFiltrados;
    ObservableList<FuncionarioRespaldado> respaldados = FXCollections.observableArrayList();
    private FilteredList<FuncionarioRespaldado> respaldadosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CheckBox checkAll = new CheckBox();
        checkAll.getStyleClass().add("negro");
        checkAll.setOnAction(e -> {
            boolean seleccionado = checkAll.isSelected();
            for (Funcionario f : tv_funcionarios.getItems()) {
                f.setSeleccionado(seleccionado);
            }
        });
        tc_check.setGraphic(checkAll);
        tc_num.setCellFactory(col -> new TableCell<Funcionario, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        tc_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_ci.setCellValueFactory(new PropertyValueFactory<>("ci"));
        tc_check.setCellValueFactory(cellData ->
                cellData.getValue().seleccionadoProperty()
        );
        tc_check.setCellFactory(col -> new TableCell<Funcionario, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private Funcionario currentFuncionario;
            {
                checkBox.getStyleClass().addAll("check-box", "verde");
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    if (currentFuncionario != null) {
                        checkBox.selectedProperty().unbindBidirectional(currentFuncionario.seleccionadoProperty());
                        currentFuncionario = null;
                    }
                    return;
                }
                Funcionario f = (Funcionario) getTableRow().getItem();
                if (f == null) {
                    setGraphic(null);
                    return;
                }
                // 🔥 desbindear el anterior
                if (currentFuncionario != null) {
                    checkBox.selectedProperty().unbindBidirectional(currentFuncionario.seleccionadoProperty());
                }
                currentFuncionario = f;
                // 🔥 bind correcto
                checkBox.selectedProperty().bindBidirectional(f.seleccionadoProperty());
                setGraphic(checkBox);
            }
        });
        icon_eliminar.setText("\ue7ad");
        icon_respaldar.setText("\ueb60");
        icon_cargar.setText("\ue9fc");
        funcionariosFiltrados = new FilteredList<>(funcionarios, f -> true);
        tv_funcionarios.setItems(funcionariosFiltrados);
        tf_busqueda.textProperty().addListener((obs, oldText, newText) -> {
            aplicarFiltro(newText);
        });
        btn_aceptar_eliminar.disableProperty().bind(
                cb_confirmar_eliminar.selectedProperty().not()
        );
        tc_num_reporte.setCellFactory(col -> new TableCell<ReporteEliminacion, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setText(null);
                else
                    setText(String.valueOf(getIndex() + 1));
            }
        });
        tc_nombre_reporte.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_mensaje_reporte.setCellValueFactory(new PropertyValueFactory<>("mensaje"));
        tc_icono.setCellValueFactory(new PropertyValueFactory<>("exito"));
        tc_icono.setCellFactory(col -> new TableCell<ReporteEliminacion, Boolean>() {
            @Override
            protected void updateItem(Boolean exito, boolean empty) {
                super.updateItem(exito, empty);
                if (empty || exito == null) {
                    setText(null);
                } else {
                    setText(exito ? "\ue86c" : "\ue5c9");
                    getStyleClass().add("icon");
                    getStyleClass().setAll(exito ? "label-verde" : "label-rojo");
                }
            }
        });

        tc_num_restaurado.setCellFactory(col -> new TableCell<ReporteEliminacion, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setText(null);
                else
                    setText(String.valueOf(getIndex() + 1));
            }
        });
        tc_nombre_restaurado.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_mensaje_restaurado.setCellValueFactory(new PropertyValueFactory<>("mensaje"));
        tc_icono_restaurado.setCellValueFactory(new PropertyValueFactory<>("exito"));
        tc_icono_restaurado.setCellFactory(col -> new TableCell<ReporteEliminacion, Boolean>() {
            @Override
            protected void updateItem(Boolean exito, boolean empty) {
                super.updateItem(exito, empty);
                if (empty || exito == null) {
                    setText(null);
                } else {
                    setText(exito ? "\ue86c" : "\ue5c9");
                    getStyleClass().add("icon");
                    getStyleClass().setAll(exito ? "label-verde" : "label-rojo");
                }
            }
        });

        respaldadosFiltrados = new FilteredList<>(respaldados, f -> true);
        tv_respaldados.setItems(respaldadosFiltrados);
        tc_check_respaldado.setCellValueFactory(cellData ->
                cellData.getValue().seleccionadoProperty()
        );
        tc_check_respaldado.setCellFactory(col -> new TableCell<FuncionarioRespaldado, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private FuncionarioRespaldado currentFuncionario;
            {
                checkBox.getStyleClass().addAll("check-box", "verde");
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    if (currentFuncionario != null) {
                        checkBox.selectedProperty().unbindBidirectional(currentFuncionario.seleccionadoProperty());
                        currentFuncionario = null;
                    }
                    return;
                }
                FuncionarioRespaldado f = (FuncionarioRespaldado) getTableRow().getItem();
                if (f == null) {
                    setGraphic(null);
                    return;
                }
                // 🔥 desbindear el anterior
                if (currentFuncionario != null) {
                    checkBox.selectedProperty().unbindBidirectional(currentFuncionario.seleccionadoProperty());
                }
                currentFuncionario = f;
                // 🔥 bind correcto
                checkBox.selectedProperty().bindBidirectional(f.seleccionadoProperty());
                setGraphic(checkBox);
            }
        });
        tc_nombre_respaldado.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_ci_respaldado.setCellValueFactory(new PropertyValueFactory<>("ci"));
        tc_num_huellas.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getCantidadHuellas())
        );
        tc_rol.setCellValueFactory(new PropertyValueFactory<>("privilegio"));
    }

    public void initData(Terminal terminal, MainController mc) throws IOException {
        this.terminal = terminal;
        this.mc = mc;
        String usuariosJson = null;
        usuariosJson = TerminalController.ejecutarScriptPython("scriptpy/usuarios.py", terminal.ip, terminal.puerto + "");
        System.out.println(usuariosJson);
        //usuariosJson = "[{\"uid\":1,\"password\":\"\",\"user_id\":\"1\",\"group_id\":\"\",\"name\":\"DENILSON\",\"privilege\":14},{\"uid\":2,\"password\":\"\",\"user_id\":\"7920529\",\"group_id\":\"\",\"name\":\"LINETH CASTELLON GOMEZ\",\"privilege\":0},{\"uid\":3,\"password\":\"\",\"user_id\":\"2396993\",\"group_id\":\"\",\"name\":\"FABIOLA SANCHEZ ORTIZ\",\"privilege\":0},{\"uid\":4,\"password\":\"\",\"user_id\":\"7996541\",\"group_id\":\"\",\"name\":\"IVAN ALCONZ LEDEZMA\",\"privilege\":0},{\"uid\":6,\"password\":\"\",\"user_id\":\"4440091\",\"group_id\":\"\",\"name\":\"YURI ARELLANO  DELGADO\",\"privilege\":0},{\"uid\":7,\"password\":\"\",\"user_id\":\"5205968\",\"group_id\":\"\",\"name\":\"SIXTO SERRUDO VARGAS\",\"privilege\":0},{\"uid\":8,\"password\":\"\",\"user_id\":\"3\",\"group_id\":\"\",\"name\":\"FREDY\",\"privilege\":14},{\"uid\":9,\"password\":\"\",\"user_id\":\"4094083\",\"group_id\":\"\",\"name\":\"CLAUDIA M ROJAS VALENCIA\",\"privilege\":0},{\"uid\":10,\"password\":\"\",\"user_id\":\"5224790\",\"group_id\":\"\",\"name\":\"NICOLAS B SAAVEDRA ROMER\",\"privilege\":0},{\"uid\":11,\"password\":\"\",\"user_id\":\"8825808\",\"group_id\":\"\",\"name\":\"ELIANA D  MAMANI HUARANC\",\"privilege\":0},{\"uid\":13,\"password\":\"\",\"user_id\":\"7884667\",\"group_id\":\"\",\"name\":\"GONZALO ROJAS ROJAS\",\"privilege\":0},{\"uid\":14,\"password\":\"\",\"user_id\":\"2899783\",\"group_id\":\"\",\"name\":\"ERASMO CORRALES\",\"privilege\":0},{\"uid\":16,\"password\":\"\",\"user_id\":\"6458199\",\"group_id\":\"\",\"name\":\"ANAHI MEYBOL SOLIZ\",\"privilege\":0},{\"uid\":17,\"password\":\"\",\"user_id\":\"7920665\",\"group_id\":\"\",\"name\":\"WALTER ERQUICIA ADRIAN\",\"privilege\":0},{\"uid\":18,\"password\":\"\",\"user_id\":\"13996188\",\"group_id\":\"\",\"name\":\"ELIUD CAMATA GONZALES\",\"privilege\":0},{\"uid\":24,\"password\":\"\",\"user_id\":\"7928174\",\"group_id\":\"\",\"name\":\"GERMAN TARQUI OLIVERA\",\"privilege\":0},{\"uid\":25,\"password\":\"\",\"user_id\":\"5276494\",\"group_id\":\"\",\"name\":\"MAGDA ORELLANA CACERES\",\"privilege\":0},{\"uid\":26,\"password\":\"\",\"user_id\":\"8834355\",\"group_id\":\"\",\"name\":\"JAVIER IQUISI CAZORLA\",\"privilege\":0},{\"uid\":27,\"password\":\"\",\"user_id\":\"4422301\",\"group_id\":\"\",\"name\":\"JAVIER MORALES MAMANI\",\"privilege\":0},{\"uid\":29,\"password\":\"\",\"user_id\":\"9426350\",\"group_id\":\"\",\"name\":\"BILLY\",\"privilege\":14},{\"uid\":30,\"password\":\"\",\"user_id\":\"5206858\",\"group_id\":\"\",\"name\":\"MARTHA MORALES REVOLLO\",\"privilege\":0},{\"uid\":31,\"password\":\"\",\"user_id\":\"5906825\",\"group_id\":\"\",\"name\":\"ROLANDO AGUILAR ROJAS\",\"privilege\":0},{\"uid\":32,\"password\":\"\",\"user_id\":\"9413936\",\"group_id\":\"\",\"name\":\"JOSE LUIS HERNANDEZ\",\"privilege\":0},{\"uid\":33,\"password\":\"\",\"user_id\":\"7907930\",\"group_id\":\"\",\"name\":\"NEYVA TORRICO QUILO\",\"privilege\":0},{\"uid\":34,\"password\":\"\",\"user_id\":\"5972630\",\"group_id\":\"\",\"name\":\"VANESSA R VASQUEZ VILLEG\",\"privilege\":0},{\"uid\":35,\"password\":\"\",\"user_id\":\"6474500\",\"group_id\":\"\",\"name\":\"JAIME BRAVO CARVAJAL\",\"privilege\":0},{\"uid\":36,\"password\":\"\",\"user_id\":\"5191910\",\"group_id\":\"\",\"name\":\"EDWIN ESPINOZA MAMANI\",\"privilege\":0},{\"uid\":37,\"password\":\"\",\"user_id\":\"5274788\",\"group_id\":\"\",\"name\":\"GRISSEL PEREZ URNA\",\"privilege\":0}]";
        JSONArray usuariosJSON = new JSONArray(usuariosJson);
        funcionarios.clear();
        for (int i = 0; i < usuariosJSON.length(); i++) {
            JSONObject u = usuariosJSON.getJSONObject(i);
            int uid = u.optInt("uid");
            int ci = u.optInt("user_id");
            String nombre = u.optString("name");
            funcionarios.add(new Funcionario(uid, ci, nombre));
        }
        bindSeleccionados();
        lbl_titulo.setText("Administrar Funcionarios " + terminal.getNombre());
        lbl_filtrados.textProperty().bind(
                Bindings.size(funcionariosFiltrados).asString()
        );

        lbl_respaldados.textProperty().bind(
                Bindings.size(respaldadosFiltrados).asString()
        );
    }

    private void aplicarFiltro(String texto) {
        String filtro = texto.toLowerCase().trim();
        funcionariosFiltrados.setPredicate(f ->
                f.getNombre().toLowerCase().contains(filtro) ||
                        String.valueOf(f.getCi()).contains(filtro)
        );
    }

    private List<Funcionario> getSeleccionados() {
        List<Funcionario> seleccionados = new ArrayList<>();
        for (Funcionario f : funcionarios) {
            if (f.isSeleccionado()) {
                seleccionados.add(f);
            }
        }
        return seleccionados;
    }

    private void bindSeleccionados() {
        funcionarios.forEach(f ->
                f.seleccionadoProperty().addListener((obs, old, val) ->
                        actualizarSeleccionados()
                )
        );
    }

    private void actualizarSeleccionados() {
        long total = getSeleccionados().size();
        lbl_seleccionados.setText(total + "");
    }

    @FXML
    public void confirmarEliminar() {
        if(getSeleccionados().size() > 0) {
            cb_confirmar_eliminar.setSelected(false);
            lv_funcionarios.setItems(FXCollections.observableArrayList(getSeleccionados()));
            mostrarVistaOverlay(vb_confirmar_eliminar);
        } else {
            ToastController toast = ToastController.createToast("info", "Información", "Debes seleccionar al menos un funcionario");
            toast.show(mc.root);
        }
    }

    @FXML
    public void cerrarEliminar() {
        overlay.setVisible(false);
    }

    @FXML
    public void eliminar() {
        StringJoiner uids = new StringJoiner(",");
        StringJoiner cis  = new StringJoiner(",");
        for (Funcionario f : getSeleccionados()) {
            uids.add(String.valueOf(f.getUid()));
            cis.add(String.valueOf(f.getCi()));
        }
        try {
            // 2. Ejecutar el script con los argumentos formateados
            System.out.println(terminal.getIp());
            String resultado = TerminalController.ejecutarScriptPython(
                    "scriptpy/eliminar_usuarios.py",
                    terminal.getIp(),
                    uids.toString(),
                    cis.toString()
            );
            JSONObject json = new JSONObject(resultado);
            boolean exito = json.getBoolean("exito");
            if(exito) {
                String mensaje = json.getString("mensaje");
                ToastController toast = ToastController.createToast("success", "Comando enviado", mensaje);
                toast.show(mc.root);
                JSONArray resultados = json.getJSONArray("resultados");
                ObservableList<ReporteEliminacion> data = FXCollections.observableArrayList();
                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject r = resultados.getJSONObject(i);
                    int uid = r.optInt("uid");
                    String nombreRes = r.optString("nombre");
                    String mensajeRes = r.optString("mensaje");
                    boolean exitoRes = r.optBoolean("exito");
                    if (exitoRes) {
                        funcionarios.removeIf(f -> f.getUid() == uid);
                    }
                    data.add(new ReporteEliminacion(uid, nombreRes, mensajeRes, exitoRes));
                }
                tv_reporte.setItems(data);
                mostrarVistaOverlay(vb_reporte_eliminacion);
            } else {
                String mensaje = json.getString("mensaje");
                ToastController toast = ToastController.createToast("error", "Error", mensaje);
                toast.show(mc.root);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar alerta de error de conexión
        }
    }

    @FXML
    public void cerrarReporte() {
        overlay.setVisible(false);
    }


    @FXML
    public void respaldar() {
        List<Funcionario> seleccionados = getSeleccionados();
        if (seleccionados.isEmpty()) {
            ToastController toast = ToastController.createToast(
                    "info",
                    "Información",
                    "Debes seleccionar al menos un funcionario"
            );
            toast.show(mc.root);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar respaldo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Respaldo (*.json)", "*.json")
        );
        File destino = fileChooser.showSaveDialog(
                btn_respaldar.getScene().getWindow()
        );
        if (destino == null) {
            return; // usuario canceló
        }
        try {
            // 1️⃣ Construir user_ids (CI)
            String userIds = seleccionados.stream()
                    .map(f -> String.valueOf(f.getCi()))
                    .collect(java.util.stream.Collectors.joining(","));
            // 2️⃣ Ejecutar script
            String json = TerminalController.ejecutarScriptPython(
                    "scriptpy/usuarios_y_huellas.py",
                    terminal.ip,
                    userIds
            );
            // 3️⃣ Guardar archivo
            guardarArchivo(destino, json);
            ToastController toast = ToastController.createToast(
                    "success",
                    "Respaldo generado",
                    "El respaldo se guardó correctamente"
            );
            toast.show(mc.root);
        } catch (Exception e) {
            ToastController toast = ToastController.createToast(
                    "error",
                    "Error",
                    "No se pudo generar el respaldo"
            );
            toast.show(mc.root);
            e.printStackTrace();
        }
    }

    @FXML
    public void cargarRespaldo() throws IOException {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar respaldo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Respaldos", "*.json", "*.gams")
        );
        File archivo = fc.showOpenDialog(btn_cargar.getScene().getWindow());
        if (archivo == null)
            return;
        else {
            ObservableList<FuncionarioRespaldado> res = parsearRespaldo(leerArchivo(archivo));
            respaldados.setAll(res);
            mostrarCargarRespaldo();
            ToastController toast = ToastController.createToast("info", "Información", archivo.toString());
            toast.show(mc.root);
        }
    }

    private String leerArchivo(File archivo) throws IOException {
        return new String(
                java.nio.file.Files.readAllBytes(archivo.toPath()),
                java.nio.charset.StandardCharsets.UTF_8
        );
    }

    private ObservableList<FuncionarioRespaldado> parsearRespaldo(String json) {
        ObservableList<FuncionarioRespaldado> lista = FXCollections.observableArrayList();
        JSONObject root = new JSONObject(json);
        JSONArray usuarios = root.getJSONArray("usuarios");

        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject u = usuarios.getJSONObject(i);
            int uidOrigen = u.optInt("uid_origen", 0);
            int ci = u.optInt("user_id", 0);
            String nombre = u.optString("name", "");
            int privilegio = u.optInt("privilege", 0);
            JSONArray huellasJson = u.optJSONArray("huellas");
            List<Huella> huellas = new ArrayList<>();
            if (huellasJson != null) {
                for (int j = 0; j < huellasJson.length(); j++) {
                    JSONObject h = huellasJson.getJSONObject(j);
                    Huella huella = new Huella(h.getInt("fid"), h.getInt("size"), h.getInt("valid"), h.getString("template"));
                    huellas.add(huella);
                }
            }
            lista.add(new FuncionarioRespaldado(uidOrigen, ci, nombre, privilegio, huellas));
        }
        return lista;
    }

    private void mostrarVistaOverlay(VBox vistaAMostrar) {
        overlay.setVisible(true);
        // Creamos un array con todas las vistas que residen en el StackPane
        VBox[] todasLasVistas = {
                vb_confirmar_eliminar,
                vb_reporte_eliminacion,
                vb_reporte_restauracion,
                vb_cargando
        };
        for (VBox v : todasLasVistas) {
            if (v == vistaAMostrar) { // Comparación de identidad (referencia)
                v.setVisible(true);
                v.setManaged(true);  // Permite que JavaFX calcule su tamaño
            } else {
                v.setVisible(false);
                v.setManaged(false); // Evita que una vista oculta afecte al diseño
            }
        }
    }

    private void guardarArchivo(File archivo, String contenido) throws IOException {
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(archivo),
                        "UTF-8"
                )
        )) {
            writer.write(contenido);
        }
    }

    @FXML
    private void mostrarCargarRespaldo() {
        vb_funcionarios.setVisible(false);
        vb_funcionarios.setManaged(false);
        vb_cargar_respaldo.setVisible(true);
        vb_cargar_respaldo.setManaged(true);
    }

    @FXML
    private void verFuncionarios() {
        vb_cargar_respaldo.setVisible(false);
        vb_cargar_respaldo.setManaged(false);
        vb_funcionarios.setVisible(true);
        vb_funcionarios.setManaged(true);
    }

    private String generarJSONRestauracion( List<FuncionarioRespaldado> seleccionados, String ipTerminal) {
        JSONObject root = new JSONObject();
        root.put("accion", "restaurar");
        root.put("terminal_ip", ipTerminal);
        JSONArray respaldados = new JSONArray();
        for (FuncionarioRespaldado f : seleccionados) {
            respaldados.put(f.toJSON());
        }
        root.put("respaldados", respaldados);
        return root.toString();
    }

    @FXML
    public void restaurarEnBiometrico() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.getDialogPane().getStylesheets()
                .add(Main.class.getResource("/styles/global.css").toExternalForm());
        confirmacion.setTitle("Confirmación");
        confirmacion.setHeaderText("¿Deseas restaurar estos funcionarios?");
        String jsonEnvio = generarJSONRestauracion(
                getSeleccionadosRespaldados(),
                terminal.ip
        );
        Label label = new Label("Se restaurarán los datos y huellas de los funcionarios seleccionados");
        label.setWrapText(true);
        label.setStyle("-fx-padding: 10;");
        confirmacion.getDialogPane().setContent(label);
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }
        // 🔹 Mostrar overlay de carga
        mostrarVistaOverlay(vb_cargando);
        // 🔹 Tarea en segundo plano
        Task<JSONObject> task = new Task<JSONObject>() {
            @Override
            protected JSONObject call() throws Exception {
                String respuesta = ejecutarScriptPythonConStdin(
                        "scriptpy/restaurar_respaldo.py",
                        jsonEnvio
                );
                return new JSONObject(respuesta);
            }
        };
        // 🔹 Cuando termina OK
        task.setOnSucceeded(e -> {
            overlay.setVisible(false);
            JSONObject json = task.getValue();
            boolean exito = json.optBoolean("exito", false);
            if (exito) {
                ToastController.createToast(
                        "success",
                        "Comando enviado",
                        json.optString("mensaje")
                ).show(mc.root);
                JSONArray resultados = json.getJSONArray("resultados");
                ObservableList<ReporteRestauracion> data = FXCollections.observableArrayList();
                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject r = resultados.getJSONObject(i);
                    data.add(new ReporteRestauracion(
                            r.optInt("ci"),
                            r.optString("nombre"),
                            r.optString("mensaje"),
                            r.optBoolean("exito")
                    ));
                }
                tv_restaurados.setItems(data);
                mostrarVistaOverlay(vb_reporte_restauracion);
            } else {
                ToastController.createToast(
                        "error",
                        "Error",
                        json.optString("mensaje")
                ).show(mc.root);
            }
        });
        // 🔹 Cuando falla
        task.setOnFailed(e -> {
            overlay.setVisible(false);
            Throwable ex = task.getException();
            ToastController.createToast(
                    "error",
                    "Error",
                    ex.getMessage()
            ).show(mc.root);
        });
        // 🔹 Ejecutar
        new Thread(task, "restaurar-biometrico-thread").start();
    }

    @FXML
    public void cerrarReporteRestauracion() {
        Stage stage = (Stage) btn_cargar.getScene().getWindow();
        stage.close();
        mc.pane_mascara.toBack();
        mc.pane_mascara.setVisible(false);
    }

    private List<FuncionarioRespaldado> getSeleccionadosRespaldados() {
        List<FuncionarioRespaldado> seleccionados = new ArrayList<>();
        for (FuncionarioRespaldado fr : respaldados) {
            if (fr.isSeleccionado()) {
                seleccionados.add(fr);
            }
        }
        return seleccionados;
    }

    public static String ejecutarScriptPythonConStdin(String scriptPath, String jsonInput) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String comandoPython = os.contains("win") ? "python" : "python3";
        ProcessBuilder pb = new ProcessBuilder(comandoPython, scriptPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        // 🔹 Enviar JSON por stdin
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
        )) {
            writer.write(jsonInput);
            writer.flush();
        }
        // 🔹 Leer respuesta
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}
