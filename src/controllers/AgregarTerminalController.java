package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import models.Terminal;

import java.net.URL;
import java.util.ResourceBundle;

public class AgregarTerminalController implements Initializable {

    @FXML
    public Label lbl_titulo;
    public TextField tf_nombre;
    public TextField tf_ip;
    public TextField tf_puerto;
    public HBox hb_mensaje;
    public Label lbl_mensaje;
    public Label ic_alerta;

    Terminal terminal= null;

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        ic_alerta.setStyle("-fx-text-fill: #ce5656;");
        ic_alerta.setText("\ue15c");
    }

    public void initData(Terminal terminal) {
        this.terminal = terminal;
        lbl_titulo.setText("Editar Terminal");
        tf_nombre.setText(terminal.nombre);
        tf_ip.setText(terminal.ip);
        tf_puerto.setText(terminal.puerto + "");
    }
}
