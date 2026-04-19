import socket
from deep_translator import GoogleTranslator

HOST = "0.0.0.0"
PORT = 9999
BUFFER_SIZE = 4096

TEMP_FILE = "temp_en.txt"
TRANSLATED_FILE = "translated.txt"


# ================= AI TRANSLATE =================
def translate_to_vietnamese(english_text: str) -> str:
    try:
        # Cắt đoạn để tránh giới hạn ~5000 ký tự
        chunks = [english_text[i:i+4000] for i in range(0, len(english_text), 4000)]

        translated_chunks = []
        for chunk in chunks:
            translated = GoogleTranslator(source="en", target="vi").translate(chunk)
            translated_chunks.append(translated)

        return " ".join(translated_chunks)

    except Exception as e:
        return f"[Lỗi dịch: {e}] {english_text}"


# ================= TCP HELPER =================
def recv_exact(conn, n):
    data = b""
    while len(data) < n:
        chunk = conn.recv(min(BUFFER_SIZE, n - len(data)))
        if not chunk:
            return None
        data += chunk
    return data


# ================= HANDLE CLIENT =================
def handle_client(conn, addr):
    try:
        raw_length = recv_exact(conn, 4)
        if not raw_length:
            return

        msg_length = int.from_bytes(raw_length, "big")

        raw_data = recv_exact(conn, msg_length)
        if not raw_data:
            return

        text = raw_data.decode("utf-8")

        # Lưu file gốc
        with open(TEMP_FILE, "w", encoding="utf-8") as f:
            f.write(text)

        # ===== GỌI AI =====
        translated = translate_to_vietnamese(text)

        # Lưu file dịch
        with open(TRANSLATED_FILE, "w", encoding="utf-8") as f:
            f.write(translated)

        # Gửi lại client
        res = translated.encode("utf-8")
        conn.sendall(len(res).to_bytes(4, "big") + res)

    finally:
        conn.close()


# ================= START SERVER =================
def start_server():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(5)

    print(f"Server chạy tại {HOST}:{PORT}")

    while True:
        conn, addr = s.accept()
        handle_client(conn, addr)


if __name__ == "__main__":
    start_server()