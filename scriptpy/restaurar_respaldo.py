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

    for r in respaldados:
        user_id = str(r["ci"])
        nombre = r["nombre"]
        privilegio = int(r.get("privilegio", 0))
        huellas = r.get("huellas", [])

        # ¿Ya existe?
        if any(u.user_id == user_id for u in usuarios):
            resultados.append({
                "ci": user_id,
                "exito": False,
                "mensaje": "El usuario ya existe en el terminal"
            })
            continue

        # Buscar UID libre
        uids_usados = sorted(u.uid for u in usuarios if isinstance(u.uid, int))
        uid = 1
        for u in uids_usados:
            if u == uid:
                uid += 1
            elif u > uid:
                break

        # Crear usuario
        try:
            conn.set_user(uid=uid, name=nombre, privilege=privilegio, user_id=user_id)
        except Exception as e:
            resultados.append({
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
                    uid=uid,
                    fid=int(h["fid"]),
                    valid=int(h.get("valid", 1)),
                    template=bytes.fromhex(h["template"])
                )

                conn.save_user_template(uid, [finger])
                time.sleep(0.3)
                huellas_ok += 1

            except Exception:
                huellas_err += 1

        resultados.append({
            "ci": user_id,
            "exito": True,
            "mensaje": (
                f"Usuario creado. "
                f"Huellas restauradas: {huellas_ok}, "
                f"fallidas: {huellas_err}"
            )
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
    "resultados": respaldados
}))