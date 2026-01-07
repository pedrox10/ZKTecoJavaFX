package models;

public class ReporteRestauracion {

    private int ci;
    private String nombre;
    private String mensaje;
    private boolean exito;

    public ReporteRestauracion(int ci, String nombre, String mensaje, boolean exito) {
        this.ci = ci;
        this.nombre = nombre;
        this.mensaje = mensaje;
        this.exito = exito;
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
