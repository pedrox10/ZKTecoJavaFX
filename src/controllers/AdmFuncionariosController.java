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
import javafx.stage.FileChooser;
import models.Funcionario;
import models.Terminal;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
        icon_respaldar.setText("\ue173");
        icon_cargar.setText("\ue9fc");

        funcionariosFiltrados = new FilteredList<>(funcionarios, f -> true);
        tv_funcionarios.setItems(funcionariosFiltrados);

        tf_busqueda.textProperty().addListener((obs, oldText, newText) -> {
            aplicarFiltro(newText);
        });

        btn_aceptar_eliminar.disableProperty().bind(
                cb_confirmar_eliminar.selectedProperty().not()
        );
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
            overlay.setVisible(true);
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
            System.out.println("Resultado de eliminación: " + resultado);
            // 3. Opcional: Refrescar la tabla o mostrar mensaje de éxito
            //actualizarTabla();
        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar alerta de error de conexión
        }
    }

    @FXML
    public void respaldar() {
        if(getSeleccionados().size() > 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar respaldo");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Respaldo (*.json)", "*.json")
            );
            File destino = fileChooser.showSaveDialog(
                    btn_respaldar.getScene().getWindow()
            );
        } else {
            ToastController toast = ToastController.createToast("info", "Información", "Debes seleccionar al menos un funcionario");
            toast.show(mc.root);
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
}
