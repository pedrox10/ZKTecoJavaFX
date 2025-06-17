package models;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.OneToMany;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Terminal extends Model<Terminal> {
    @PrimaryKey(autoIncrement = true)
    public int id;
    public String ip;
    public String nombre;
    public int puerto;
    public String numSerie;
    public String modelo;
    public int totalMarcaciones;
    @OneToMany(toType = Respaldo.class, onField = "terminal")
    public EntityList<Terminal, Respaldo> respaldos = new EntityList(Terminal.class, Respaldo.class, this);

    public Terminal() {
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPuerto() { return puerto; }
    public void setPuerto(int puerto) { this.puerto = puerto; }

    public Respaldo getUltimoRespaldo() {
        Respaldo res = null;
        if (respaldos != null && !respaldos.isEmpty()) {
            res = respaldos.get(respaldos.size() - 1);;
        }
        return res;
    }

    public String getNumSerie() {
        return numSerie;
    }

    public void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getTotalMarcaciones() {
        return totalMarcaciones;
    }

    public void setTotalMarcaciones(int totalMarcaciones) {
        this.totalMarcaciones = totalMarcaciones;
    }

    @Override
    public String toString() {
        return "Terminal{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ip='" + ip + '\'' +
                ", puerto=" + puerto +
                '}';
    }
}
