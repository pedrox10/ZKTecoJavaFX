package controllers;

import components.terminal.TerminalController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import models.Funcionario;
import models.Respaldo;
import models.Terminal;
import models.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdmFuncionariosController implements Initializable {

    @FXML
    public TableView<Funcionario> tv_funcionarios;
    public TableColumn<Funcionario, Boolean> tc_check;
    public TableColumn tc_num;
    public TableColumn tc_nombre;
    public TableColumn tc_ci;

    Terminal terminal= null;
    MainController mc = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tv_funcionarios.setEditable(true);
        tc_check.setEditable(true);
        CheckBox checkAll = new CheckBox();
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
        tc_check.setCellFactory(CheckBoxTableCell.forTableColumn(tc_check));
    }

    public void initData(Terminal terminal, MainController mc) {
        this.terminal = terminal;
        this.mc = mc;
        String usuariosJson = null;
        try {
            usuariosJson = TerminalController.ejecutarScriptPython("scriptpy/usuarios.py", terminal.ip, terminal.puerto + "");
            System.out.println(usuariosJson);
            JSONArray usuariosJSON = new JSONArray(usuariosJson);
            List<Funcionario> funcionarios = new ArrayList<>();
            for (int i = 0; i < usuariosJSON.length(); i++) {
                JSONObject u = usuariosJSON.getJSONObject(i);

                int uid = u.optInt("uid");
                int ci = u.optInt("user_id");
                String nombre = u.optString("name");

                funcionarios.add(new Funcionario(uid, ci, nombre));
            }

            tv_funcionarios.getItems().setAll(funcionarios);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //TerminalController.ejecutarScriptPython()

    }
}
