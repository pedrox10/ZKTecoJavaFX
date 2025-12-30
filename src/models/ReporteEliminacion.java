package models;

public class ReporteEliminacion {

    private int uid;
    private String nombre;
    private String mensaje;
    private boolean exito;

    public ReporteEliminacion(int uid, String nombre, String mensaje, boolean exito) {
        this.uid = uid;
        this.nombre = nombre;
        this.mensaje = mensaje;
        this.exito = exito;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }
}
