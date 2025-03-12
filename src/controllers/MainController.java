package controllers;

import app.Main;
import components.terminal.TerminalController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    private FlowPane terminalContainer;
    @FXML
    private Button btn_agregar;
    ObjectProperty<StackPane> op_root = new SimpleObjectProperty<StackPane>();
    StackPane root;

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        op_root.addListener(new ChangeListener<StackPane>() {
            @Override
            public void changed(ObservableValue<? extends StackPane> observable, StackPane oldValue, StackPane newValue) {
                if (newValue != null) {
                    root = newValue;
                    btn_agregar.setText("\ue147");
                    List<Terminal> terminales = Model.fetchAll(Terminal.class);
                    try {
                        for (Terminal terminal : terminales) {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/terminal/Terminal.fxml"));
                            Parent terminalNode = loader.load();
                            // Obtener el controlador y pasarle los datos
                            TerminalController controller = loader.getController();
                            controller.setTerminalData(terminal, root);
                            terminalContainer.getChildren().add(terminalNode);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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
        // Obtener el controlador y pasarle los datos
        AgregarTerminalController controller = loader.getController();
        AnchorPane root = (AnchorPane) loader.getRoot();
        dialogo.getDialogPane().getStylesheets().add(Main.class.getResource("/styles/global.css").toExternalForm());
        dialogo.getDialogPane().setContent(root);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.show();
        pane_mascara.setVisible(true);
        pane_mascara.toFront();

        Button btn_ok = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        btn_ok.addEventFilter(ActionEvent.ACTION, (ae) -> {
            System.out.println("OK");
        });
        dialogo.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                pane_mascara.toBack();
                pane_mascara.setVisible(false);
            }
        });
    }
}
