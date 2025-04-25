package models;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Configuracion extends Model<Configuracion> {
    @PrimaryKey(autoIncrement = true)
    public int id;
    public String urlServer;

    public Configuracion() {}

    public Configuracion(String urlServer) {
        this.urlServer = urlServer;
    }

    public String getUrlServer() {
        return urlServer;
    }

    public void setUrlServer(String urlServer) {
        this.urlServer = urlServer;
    }
}