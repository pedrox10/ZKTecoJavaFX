import sys, json, time
from zk import ZK
from zk.finger import Finger

data = json.loads(sys.stdin.read())
ip = data["terminal_ip"]
respaldados = data["respaldados"]
zk = ZK(ip, port=4370, timeout=10)
conn = None
resultados = []
try:
    conn = zk.connect()
    usuarios = conn.get_users()
    uids_usados = {u.uid for u in usuarios if isinstance(u.uid, int)}
    for r in respaldados:
        user_id = str(r["ci"])
        nombre = r["nombre"]
        privilegio = int(r.get("privilegio", 0))
        huellas = r.get("huellas", [])
        # ¿Ya existe?
        if any(u.user_id == user_id for u in usuarios):
            resultados.append({
                "nombre": nombre,
                "ci": user_id,
                "exito": False,
                "mensaje": "El usuario ya existe en el terminal"
            })
            continue
        # Buscar UID libre
        uid_libre = 1
        while uid_libre in uids_usados:
            uid_libre += 1
        # Crear usuario
        try:
            conn.set_user(uid=uid_libre, name=nombre, privilege=privilegio, user_id=user_id)
            uids_usados.add(uid_libre)
        except Exception as e:
            resultados.append({
                "nombre": nombre,
                "ci": user_id,
                "exito": False,
                "mensaje": f"Error al crear usuario: {str(e)}"
            })
            continue
        # Espera crítica
        time.sleep(1.5)
        conn.refresh_data()
        huellas_ok = 0
        huellas_err = 0
        for h in huellas:
            try:
                finger = Finger(
                    uid=uid_libre,
                    fid=int(h["fid"]),
                    valid=int(h.get("valid", 1)),
                    template=bytes.fromhex(h["template"])
                )
                conn.save_user_template(uid_libre, [finger])
                time.sleep(0.3)
                huellas_ok += 1
            except Exception:
                huellas_err += 1
        resultados.append({
            "nombre": nombre,
            "ci": user_id,
            "exito": True,
            "mensaje": f"Huellas restauradas: {huellas_ok}, fallidas: {huellas_err}"
        })
except Exception as e:
    print(json.dumps({
        "accion": "restaurar",
        "exito": False,
        "mensaje": f"No se pudo conectar al terminal: {str(e)}"
    }))
    sys.exit(1)
finally:
    if conn:
        conn.disconnect()
print(json.dumps({
    "accion": "restaurar",
    "exito": True,
    "mensaje": "Comando ejecutado correctamente.\nRevisa los resultados por funcionario.",
    "resultados": resultados
}))