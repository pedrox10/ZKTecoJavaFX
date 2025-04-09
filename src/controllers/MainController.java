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
import models.Respaldo;
import models.Terminal;
import org.orman.mapper.Model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

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
            String nombre = atc.tf_nombre.getText().trim();
            String ip = atc.tf_ip.getText().trim();
            String puerto = atc.tf_puerto.getText().trim();
            String respuesta = validarDatos(nombre, ip, puerto);
            if( respuesta != "Correcto") {
                ae.consume();
                atc.hb_mensaje.setVisible(true);
                atc.lbl_mensaje.setText(respuesta);
            } else {
                Terminal terminal = new Terminal();
                terminal.nombre = nombre;
                terminal.ip = ip;
                terminal.puerto = Integer.parseInt(puerto);
                terminal.insert();
                agregarTerminalUI(terminal);
            }
        });
        dialogo.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                pane_mascara.toBack();
                pane_mascara.setVisible(false);
            }
        });
    }

    public String validarDatos(String nombre, String ip, String puertoStr) {
        if (nombre.isEmpty()) {
            return "El nombre no puede estar vacio";
        }
        if ( nombre.length() > 14) {
            return "El nombre debe tener máximo 14 caracteres.";
        }
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        if (!Pattern.matches(ipRegex, ip) || ip.isEmpty() ) {
            return "IP no válida.";
        }
        if (!puertoStr.matches("\\d+") || puertoStr.isEmpty() ) {
            return "El puerto debe ser un número";
        }
        return "Correcto";
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

    public void editarTerminalUI(Terminal terminal) {
        for (Node n : fp_terminales.getChildren()) {
            StackPane stackPane = (StackPane) n; // Se asume que cada terminal es un StackPane
            Terminal t = (Terminal) stackPane.getUserData();
            if (t.id == terminal.id) {
                // Buscar el VBox con id "id_cabecera" dentro del StackPane
                for (Node child : stackPane.getChildren()) {
                    if (child instanceof VBox) {
                        VBox vboxCabecera = (VBox) child;
                        if ("vb_cabecera".equals(vboxCabecera.getId())) {
                            // Buscar y actualizar los Labels dentro del VBox
                            for (Node vboxChild : vboxCabecera.getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    String id = label.getId();
                                    if (id != null) { // Evitar NullPointerException
                                        switch (id) {
                                            case "lbl_nombre":
                                                label.setText(terminal.nombre);
                                                break;
                                            case "lbl_ip":
                                                label.setText(terminal.ip);
                                                break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                stackPane.setUserData(terminal);
                break;
            }
        }
    }

    public void eliminarTerminalUI(Terminal terminal) {
        for (Node n : fp_terminales.getChildren()) {
            StackPane sp_raiz = (StackPane) n;
            Terminal t = (Terminal) sp_raiz.getUserData();
            if (t.id == terminal.id) {
                fp_terminales.getChildren().remove(sp_raiz);
                break;
            }
        }

        String basePath = Paths.get("").toAbsolutePath().toString();
        String relativePath = "Backups";
        terminal.respaldos.refreshList();
        for (Respaldo respaldo : terminal.respaldos) {
            String fullPathToDelete = basePath + File.separator + relativePath + File.separator + respaldo.nombre;
            System.out.println(fullPathToDelete);
            File file = new File(fullPathToDelete);
            System.out.println(file.getAbsolutePath());
            try {
                if (file.exists()) {
                    System.out.println("existe");
                    Files.delete(file.toPath());
                } else {
                    System.out.println("No existe: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("Error al eliminar: " + file.getAbsolutePath());
                e.printStackTrace();
            }
            respaldo.delete();
        }
        terminal.delete();
    }
}
