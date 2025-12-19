package controllers;

import app.Main;
import components.terminal.TerminalController;
import components.toast.ToastController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import models.Funcionario;
import models.Respaldo;
import models.Terminal;
import models.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
        overlay.setVisible(true);
    }

    @FXML
    public void cerrarEliminar() {
        overlay.setVisible(false);
    }

    @FXML
    public void respaldar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar respaldo");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Respaldo (*.json)", "*.json")
        );

        File destino = fileChooser.showSaveDialog(
                btn_respaldar.getScene().getWindow()
        );
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
