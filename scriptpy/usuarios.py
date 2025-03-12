from zk import ZK, const
import sys, getopt, json, datetime
# Parámetros de conexión
zk_ip = sys.argv[1] # Dirección IP de tu dispositivo ZKTeco +1 por piso
zk_port = int(sys.argv[2])     # Puerto por defecto de los dispositivos ZKTeco
timeout = 60        # Ti1

zk = ZK(zk_ip, port=zk_port, timeout=timeout)

try:
    conn = zk.connect()
    users = conn.get_users()
    aux = []
    for user in users:
        data = {
        "uid": user.uid,
        "user_id": user.user_id,
        "name": user.name,
        "privilege": user.privilege,
        "password": user.password,
        "group_id": user.group_id
        }
        aux.append(data)
    print(json.dumps(aux))

    conn.disconnect()

except Exception as e:
    print(f"Error: {e}")
