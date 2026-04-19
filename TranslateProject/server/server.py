import socket
import threading
from config import HOST, PORT
from handler import handle_client

def start_server():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(5)

    print(f"Server chạy tại {HOST}:{PORT}")

    while True:
        conn, addr = s.accept()

        t = threading.Thread(
            target=handle_client,
            args=(conn, addr),
            daemon=True
        )
        t.start()

if __name__ == "__main__":
    start_server()