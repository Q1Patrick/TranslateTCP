import socket
from config import BUFFER_SIZE

def recv_exact(conn: socket.socket, n: int):
    data = b""
    while len(data) < n:
        chunk = conn.recv(min(BUFFER_SIZE, n - len(data)))
        if not chunk:
            return None
        data += chunk
    return data

def recv_block(conn: socket.socket):
    raw_len = recv_exact(conn, 4)
    if raw_len is None:
        return None
    length = int.from_bytes(raw_len, "big")
    return recv_exact(conn, length)

def send_block(conn: socket.socket, data: bytes):
    conn.sendall(len(data).to_bytes(4, "big") + data)