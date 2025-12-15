package controllers;

import javafx.fxml.Initializable;
import models.Terminal;

import java.net.URL;
import java.util.ResourceBundle;

public class AdmFuncionariosController implements Initializable {

    Terminal terminal= null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void initData(Terminal terminal) {
        this.terminal = terminal;
    }
}
