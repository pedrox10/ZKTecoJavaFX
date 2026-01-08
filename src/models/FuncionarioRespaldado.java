package models;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class FuncionarioRespaldado extends Funcionario {

    private List<Huella> huellas;

    public FuncionarioRespaldado(int uid, int ci, String nombre, int privilegio, List<Huella> huellas) {
        super(uid, ci, nombre, privilegio);
        this.huellas = huellas;
    }

    public List<Huella> getHuellas() {
        return huellas;
    }

    public int getCantidadHuellas() {
        return huellas != null ? huellas.size() : 0;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("uid_origen", getUid());
        json.put("ci", String.valueOf(getCi()));
        json.put("nombre", getNombre());
        json.put("privilegio", getPrivilegio());

        JSONArray huellasArray = new JSONArray();
        if (getHuellas() != null) {
            for (Huella h : getHuellas()) {
                JSONObject jh = new JSONObject();
                jh.put("fid", h.getFid());
                jh.put("template", h.getTemplate());
                huellasArray.put(jh);
            }
        }
        json.put("huellas", huellasArray);
        return json;
    }
}
