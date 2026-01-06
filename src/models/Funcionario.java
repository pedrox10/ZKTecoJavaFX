package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Funcionario {

    private final BooleanProperty seleccionado = new SimpleBooleanProperty(false);
    private int uid;
    private int ci;
    private String nombre;

    public Funcionario(int uid, int ci, String nombre) {
        this.uid = uid;
        this.ci = ci;
        this.nombre = nombre;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCi() {
        return ci;
    }

    public void setCi(int ci) {
        this.ci = ci;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BooleanProperty seleccionadoProperty() {
        return seleccionado;
    }

    public boolean isSeleccionado() {
        return seleccionado.get();
    }

    public void setSeleccionado(boolean value) {
        seleccionado.set(value);
    }

    @Override
    public String toString() {
        return nombre;
    }
}