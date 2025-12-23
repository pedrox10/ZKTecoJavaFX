import sys
import json
from zk import ZK

# --- Validación de argumentos ---
# Uso: eliminar_usuarios.py <ip_terminal> <uids> [<user_ids>]
if len(sys.argv) < 3:
    print(json.dumps({
        "accion": "eliminar",
        "exito": False,
        "tipo": "is-danger",
        "mensaje": "Uso incorrecto. Formato: eliminar_usuarios.py <ip_terminal> <uids> [<user_ids>]",
        "excepcion": "Argumentos faltantes"
    }))
    sys.exit(0)

ip = sys.argv[1]
uids = [int(u) for u in sys.argv[2].split(",")]
user_ids_esperados = sys.argv[3].split(",")

def eliminar_usuarios_del_terminal(ip_terminal, uids_a_eliminar, ids_esperados):
    zk = ZK(ip_terminal, port=4370, timeout=10)
    conn = None
    resultados = []

    try:
        conn = zk.connect()
        if not conn:
            raise Exception(f"Connection failure: can't reach device ({ip_terminal})")

        usuarios = conn.get_users()
        uids_existentes = {u.uid: u for u in usuarios}

        for i, uid in enumerate(uids_a_eliminar):
            esperado = ids_esperados[i] if i < len(ids_esperados) else ""
            usuario = uids_existentes.get(uid)

            if not usuario:
                resultados.append({
                    "uid": uid,
                    "nombre": f"CI: {esperado}",
                    "exito": False,
                    "mensaje": "Ya no existe en el terminal"
                })
                continue

            # Validar identidad si se envió user_id esperado
            if esperado and usuario.user_id.strip() != esperado.strip():
                resultados.append({
                    "uid": uid,
                    "nombre": usuario.name or f"CI {esperado}",
                    "exito": False,
                    "mensaje": f"UID {uid} pertenece a otro usuario ({usuario.user_id}), no se eliminó."
                })
                continue

            try:
                conn.delete_user(uid=uid)
                resultados.append({
                    "uid": uid,
                    "nombre": usuario.name or f"CI {esperado}",
                    "exito": True,
                    "mensaje": "Eliminado correctamente"
                })
            except Exception as e:
                resultados.append({
                    "uid": uid,
                    "nombre": usuario.name or f"CI {esperado}",
                    "exito": False,
                    "mensaje": f"Error al eliminar: {str(e)}"
                })

        return {
            "accion": "eliminar",
            "exito": True,
            "tipo": "is-success",
            "mensaje": "Comando ejecutado correctamente. Revisa los resultados por funcionario.",
            "resultados": resultados
        }

    except Exception as e:
        msg = str(e)
        if "Connection failure" in msg or "can't reach device" in msg:
            return {
                "accion": "eliminar",
                "exito": False,
                "tipo": "is-warning",
                "mensaje": f"No se pudo conectar al terminal ({ip_terminal}).",
                "excepcion": msg
            }

        return {
            "accion": "eliminar",
            "exito": False,
            "tipo": "is-danger",
            "mensaje": "Ocurrió un error inesperado durante la eliminación.",
            "excepcion": msg
        }

    finally:
        if conn:
            conn.disconnect()

try:
    resultado = eliminar_usuarios_del_terminal(ip, uids, user_ids_esperados)
    print(json.dumps(resultado))
except Exception as e:
    print(json.dumps({
        "accion": "eliminar",
        "exito": False,
        "tipo": "is-danger",
        "mensaje": "Error crítico fuera de la función principal.",
        "excepcion": str(e)
    }))
sys.exit(0)