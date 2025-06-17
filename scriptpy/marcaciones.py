from zk import ZK, const
import sys, getopt, json, datetime
# Parámetros de conexión
zk_ip = sys.argv[1]     # Dirección IP de tu dispositivo ZKTeco +1 por piso
zk_port = int(sys.argv[2])      # Puerto por defecto de los dispositivos ZKTeco
if len(sys.argv) > 3:
    fecha_desde = sys.argv[3]
else:
    fecha_desde = ""
timeout = 60              # Tiempo de espera para la conexión

zk = ZK(zk_ip, port=zk_port, timeout=timeout)
# Define a custom function to serialize datetime objects
def serialize_datetime(obj):
    if isinstance(obj, datetime.datetime):
        return obj.isoformat()
    raise TypeError("Type not serializable")
try:
    conn = zk.connect()
    attendance_records = conn.get_attendance()
    serial_number = conn.get_serialnumber()
    modelo = conn.get_device_name()
    current_time = serialize_datetime(conn.get_time())
    marcaciones = []
    if fecha_desde == "":
        marcaciones = attendance_records
    else:
        datetime_object = datetime.datetime.strptime(fecha_desde, '%m/%d/%y %H:%M:%S')
        marcaciones = [marcacion for marcacion in attendance_records if marcacion.timestamp >= datetime_object]

    aux = []
    for record in marcaciones:
        data = {
        "user_id": record.user_id,
        "timestamp": serialize_datetime(record.timestamp)
        }
        aux.append(data)
    resultado = {
        "total_marcaciones": len(attendance_records),
        "numero_serie": serial_number,
        "modelo": modelo,
        "hora_terminal": current_time,
        "marcaciones": aux
    }
    print(json.dumps(resultado))
    conn.disconnect()
except Exception as e:
    print(f"Error: {e}")