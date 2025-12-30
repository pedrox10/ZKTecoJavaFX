package controllers;

import components.terminal.TerminalController;
import components.toast.ToastController;
import javafx.beans.binding.Bindings;
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
import models.Funcionario;
import models.ReporteEliminacion;
import models.Terminal;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;

public class AdmFuncionariosController implements Initializable {

    @FXML
    public TableView<Funcionario> tv_funcionarios;
    public TableColumn<Funcionario, Boolean> tc_check;
    public TableColumn tc_num;
    public TableColumn tc_nombre;
    public TableColumn tc_ci;
    public VBox vb_confirmar_eliminar;
    public VBox vb_reporte_eliminacion;
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

    Terminal terminal= null;
    MainController mc = null;
    ObservableList<Funcionario> funcionarios = FXCollections.observableArrayList();
    private FilteredList<Funcionario> funcionariosFiltrados;

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
                        checkBox.selectedProperty()
                                .unbindBidirectional(currentFuncionario.seleccionadoProperty());
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
                    checkBox.selectedProperty()
                            .unbindBidirectional(currentFuncionario.seleccionadoProperty());
                }
                currentFuncionario = f;
                // 🔥 bind correcto
                checkBox.selectedProperty()
                        .bindBidirectional(f.seleccionadoProperty());
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
    }

    public void initData(Terminal terminal, MainController mc) {
        this.terminal = terminal;
        this.mc = mc;
        String usuariosJson = null;
        try {
            usuariosJson = TerminalController.ejecutarScriptPython("scriptpy/usuarios.py", terminal.ip, terminal.puerto + "");
            System.out.println(usuariosJson);
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
            lbl_filtrados.textProperty().bind(
                    Bindings.size(funcionariosFiltrados).asString()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void cargarRespaldo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar respaldo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Respaldos", "*.json", "*.gams")
        );
        File archivo = fc.showOpenDialog(btn_cargar.getScene().getWindow());
        if (archivo == null)
            return;
        else {
            ToastController toast = ToastController.createToast("info", "Información", archivo.toString());
            toast.show(mc.root);
        }
    }

    private void mostrarVistaOverlay(VBox vistaAMostrar) {
        overlay.setVisible(true);
        // Creamos un array con todas las vistas que residen en el StackPane
        VBox[] todasLasVistas = {
                vb_confirmar_eliminar,
                vb_reporte_eliminacion
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
}
