package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ListarRespaldosController implements Initializable {

    @FXML
    public TableView tv_respaldos;
    public TableColumn tc_fecha;
    public TableColumn tc_nombre;
    public TableColumn tc_sincronizado;

    @Override
    public void initialize(URL event, ResourceBundle rb) {

    }
}
