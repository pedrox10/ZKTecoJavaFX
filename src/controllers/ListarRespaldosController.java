package controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Respaldo;

import java.net.URL;
import java.util.ResourceBundle;

public class ListarRespaldosController implements Initializable {

    @FXML
    public TableView tv_respaldos;
    public TableColumn tc_fecha;
    public TableColumn tc_nombre;
    public TableColumn tc_sincronizado;
   // ObservableList<Respaldo> respaldos = FXCollections.observableArrayList();
    //ObjectProperty<Respaldo> op_seleccionado = new SimpleObjectProperty();

    @Override
    public void initialize(URL event, ResourceBundle rb) {

    }
}
