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

}
