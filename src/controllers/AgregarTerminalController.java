package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AgregarTerminalController implements Initializable {

    @FXML
    public TextField tf_nombre;
    public TextField tf_ip;
    public TextField tf_puerto;
    public Label lb_mensaje;

    @Override
    public void initialize(URL event, ResourceBundle rb) {

    }
}
