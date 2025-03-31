package controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Respaldo;
import models.Terminal;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;

import java.net.URL;
import java.util.ResourceBundle;

public class ListarRespaldosController implements Initializable {

    @FXML
    public VBox vb_primer_paso;
    public VBox vb_segundo_paso;
    public TableView tv_respaldos;
    public TableColumn tc_fecha;
    public TableColumn tc_nombre;
    public TableColumn tc_sincronizado;
    public Button btn_siguiente;

    ObjectProperty<Respaldo> op_respaldo = new SimpleObjectProperty<>();
    ObservableList<Respaldo> respaldos = FXCollections.observableArrayList();
    Terminal terminal = null;
    MainController mc = null;

    @Override
    public void initialize(URL event, ResourceBundle rb) {
        tc_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tc_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tc_sincronizado.setCellValueFactory(new PropertyValueFactory<>("fueSincronizado"));
        tv_respaldos.setRowFactory(tv -> new TableRow<Respaldo>() {
            @Override
            protected void updateItem(Respaldo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setDisable(false);
                    setStyle(""); // Restablece el estilo
                } else {
                    if (item.fueSincronizado) { // Suponiendo que tienes un método `isSincronizado()`
                        setDisable(true);
                        setStyle("-fx-opacity: 0.65;"); // Aplica un estilo visual para indicar que está deshabilitado
                    } else {
                        setDisable(false);
                        setStyle("-fx-opacity: 1;"); // Restablece si no está sincronizado
                    }
                }
            }
        });
        tv_respaldos.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Respaldo>() {
            @Override
            public void changed(ObservableValue<? extends Respaldo> observable, Respaldo oldValue, Respaldo newValue) {
                if(newValue != null) {
                    op_respaldo.setValue(newValue);
                } else {
                    op_respaldo.setValue(null);
                }
            }
        });
        btn_siguiente.disableProperty().bind(op_respaldo.isNull());
    }

    public void initData(Terminal terminal, MainController mc) {
        this.terminal = terminal;
        this.mc = mc;
        respaldos = FXCollections.observableArrayList(Model.fetchQuery(ModelQuery.select().
                from(Respaldo.class).where(C.eq("terminal", terminal.id)).
                getQuery(), Respaldo.class));
        tv_respaldos.setItems(respaldos);
    }

    @FXML
    public void cerrarDialog(ActionEvent event) {
        Stage stage = (Stage) tv_respaldos.getScene().getWindow(); // Obtiene la ventana del diálogo
        stage.close();
        mc.pane_mascara.toBack();
        mc.pane_mascara.setVisible(false);
    }

    @FXML
    public void irSiguiente(ActionEvent event) {
        vb_primer_paso.setVisible(false);
        vb_segundo_paso.setVisible(true);
    }

    @FXML
    public void irAtras(ActionEvent event) {
        vb_primer_paso.setVisible(true);
        vb_segundo_paso.setVisible(false);
    }

}
