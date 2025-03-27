package controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Respaldo;
import models.Terminal;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;
import org.orman.sql.Query;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListarRespaldosController implements Initializable {

    @FXML
    public TableView tv_respaldos;
    public TableColumn tc_fecha;
    public TableColumn tc_nombre;
    public TableColumn tc_sincronizado;
    ObservableList<Respaldo> respaldos = FXCollections.observableArrayList();
    Terminal terminal = null;

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
    }

    public void initData(Terminal terminal) {
        this.terminal = terminal;
        respaldos = FXCollections.observableArrayList(Model.fetchQuery(ModelQuery.select().
                from(Respaldo.class).where(C.eq("terminal", terminal.id)).
                getQuery(), Respaldo.class));
        tv_respaldos.setItems(respaldos);
    }
}
