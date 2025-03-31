package controllers;

import app.Main;
import components.terminal.TerminalController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import models.Terminal;
import org.orman.mapper.Model;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    public Pane pane_mascara;
    @FXML
    private FlowPane fp_terminales;
    @FXML
    private Button btn_agregar;
    ObjectProperty<StackPane> op_root = new SimpleObjectProperty<StackPane>();
    StackPane root;
    ObservableList<Terminal> terminales = FXCollections.observableArrayList();

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        op_root.addListener(new ChangeListener<StackPane>() {
            @Override
            public void changed(ObservableValue<? extends StackPane> observable, StackPane oldValue, StackPane newValue) {
                if (newValue != null) {
                    root = newValue;
                    btn_agregar.setText("\ue147");
                    terminales = FXCollections.observableArrayList(Model.fetchAll(Terminal.class));
                    for (Terminal terminal : terminales) {
                        agregarTerminalUI(terminal);
                    }

                }
            }
        });
    }

    public void setRoot(StackPane root) {
        op_root.setValue(root);
    }

    @FXML
    private void agregarTerminal(ActionEvent event) throws IOException{
        Dialog dialogo = new Dialog();
        dialogo.setTitle("Nuevo Terminal");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AgregarTerminal.fxml"));
        Parent terminalNode = loader.load();
        //Obtener el controlador y pasarle los datos
        AgregarTerminalController atc = loader.getController();
        AnchorPane root = (AnchorPane) loader.getRoot();
        dialogo.getDialogPane().getStylesheets().add(Main.class.getResource("/styles/global.css").toExternalForm());
        dialogo.getDialogPane().setContent(root);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ButtonBar buttonBar = (ButtonBar) dialogo.getDialogPane().lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_LINUX); // "Cancelar" antes que "Aceptar"
        }
        dialogo.show();
        pane_mascara.setVisible(true);
        pane_mascara.toFront();

        Button btn_ok = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        btn_ok.addEventFilter(ActionEvent.ACTION, (ae) -> {
            System.out.println("OK");
            Terminal terminal = new Terminal();
            terminal.nombre = atc.tf_nombre.getText();
            terminal.ip = atc.tf_ip.getText();
            terminal.puerto = Integer.parseInt(atc.tf_puerto.getText());
            terminal.insert();
            agregarTerminalUI(terminal);
        });
        dialogo.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                pane_mascara.toBack();
                pane_mascara.setVisible(false);
            }
        });
    }

    public void agregarTerminalUI(Terminal terminal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/terminal/Terminal.fxml"));
            Parent terminalNode = loader.load();
            TerminalController controller = loader.getController();
            terminalNode.setUserData(terminal);
            controller.setTerminalData(terminal, root, this);
            fp_terminales.getChildren().add(terminalNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void eliminarTerminalUI(Terminal terminal) {
        for (Node n : fp_terminales.getChildren()) {
            VBox vbox = (VBox) n;
            Terminal t = (Terminal) vbox.getUserData();
            if (t.id == terminal.id) {
                fp_terminales.getChildren().remove(vbox);
                break;
            }
        }
    }
}
