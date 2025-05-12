package app;

import models.Configuracion;
import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

public class AppConfig {
    private static final String DEFAULT_URL = "http://10.0.38.71:4000/api";

    public static String getUrlServidor() {
        Configuracion config = Model.fetchSingle(ModelQuery.select().from(Configuracion.class).getQuery() , Configuracion.class);
        if (config == null) {
            config = new Configuracion(DEFAULT_URL);
            config.insert();
        }
        return config.getUrlServer();
    }

    public static void actualizarUrlServidor(String nuevaUrl) {
        Configuracion config = Model.fetchSingle(ModelQuery.select().from(Configuracion.class).getQuery(), Configuracion.class);
        if (config == null) {
            config = new Configuracion(nuevaUrl);
            config.insert();
        } else {
            config.setUrlServer(nuevaUrl);
            config.update();
        }
    }
}