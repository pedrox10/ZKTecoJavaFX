package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Funcionario {

    private final BooleanProperty seleccionado = new SimpleBooleanProperty(false);

    private int uid;
    private int ci;
    private String nombre;

    public BooleanProperty seleccionadoProperty() {
        return seleccionado;
    }

    public boolean isSeleccionado() {
        return seleccionado.get();
    }

    public void setSeleccionado(boolean value) {
        seleccionado.set(value);
    }

    // getters/setters restantes
}