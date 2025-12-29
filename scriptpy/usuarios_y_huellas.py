from zk import ZK
import sys, json

zk_ip = sys.argv[1]
zk_port = 4370
timeout = 30

user_ids_filtrar = []
if len(sys.argv) > 2 and sys.argv[2].strip():
    user_ids_filtrar = [
        u.strip() for u in sys.argv[2].split(",")
        if u.strip()
    ]

zk = ZK(zk_ip, port=zk_port, timeout=timeout)

resultado = {
    "accion": "respaldo",
    "usuarios": []
}

try:
    conn = zk.connect()
    users = conn.get_users()

    for user in users:
        # 🔹 Filtrar por USER_ID (CI)
        if user_ids_filtrar and user.user_id not in user_ids_filtrar:
            continue

        huellas = []

        for fid in range(10):
            try:
                tpl = conn.get_user_template(uid=user.uid, temp_id=fid)
                if tpl and tpl.template and tpl.valid == 1:
                    huellas.append({
                        "fid": fid,
                        "size": tpl.size,
                        "valid": tpl.valid,
                        "template": tpl.template.hex()
                    })
            except Exception:
                continue

        resultado["usuarios"].append({
            "uid_origen": user.uid,        # solo informativo
            "user_id": user.user_id,       # 🔑 identidad real
            "name": user.name,
            "privilege": user.privilege,
            "group_id": user.group_id,
            "huellas": huellas
        })

    conn.disconnect()
    print(json.dumps(resultado))

except Exception as e:
    print(json.dumps({
        "accion": "respaldo",
        "exito": False,
        "error": str(e)
    }))