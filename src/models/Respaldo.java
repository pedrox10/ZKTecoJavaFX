package models;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import java.util.Date;

@Entity
public class Respaldo extends Model<Respaldo> {
    @PrimaryKey(autoIncrement = true)
    public int id;
    public Date fecha;
    public String nombre;
    public Terminal terminal;
    public boolean fueSincronizado;

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public boolean isFueSincronizado() {
        return fueSincronizado;
    }

    public void setFueSincronizado(boolean fueSincronizado) {
        this.fueSincronizado = fueSincronizado;
    }
}

