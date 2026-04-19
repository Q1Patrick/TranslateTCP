import json
import os
from config import UPLOAD_DIR, DOWNLOAD_DIR, LANG_NAMES
from translator import translate_text
from network import recv_block, send_block
import socket
# ================= HANDLE CLIENT =================
def handle_client(conn: socket.socket, addr: tuple):  # tuple ->  example : ("192.0.0.0",54321)
    client_id = f"{addr[0]}:{addr[1]}"
    print(f"\n{'='*50}")
    print(f"[+] Client kết nối: {client_id}")

    try:
        # BƯỚC 1: Nhận HEADER JSON
        # { "lang": "vi", "filename": "myfile.txt" }

        header_bytes = recv_block(conn)
        if header_bytes is None:
            print(f"[-] {client_id}: Không nhận được header.")
            return

        header = json.loads(header_bytes.decode("utf-8"))
        target_lang = header.get("lang", "vi")
        orig_filename = header.get("filename", "file.txt")

        print(f"[→] File    : {orig_filename}")
        print(f"[→] Dịch sang: {LANG_NAMES.get(target_lang, target_lang)}")


        # BƯỚC 2: Nhận nội dung file gốc

        file_bytes = recv_block(conn)
        if file_bytes is None:
            print(f"[-] {client_id}: Không nhận được nội dung file.")
            return

        original_text = file_bytes.decode("utf-8")
        print(f"[→] Nhận xong: {len(file_bytes)} bytes ({len(original_text)} ký tự)")

        # Lưu file gốc vào thư mục uploads/
        upload_path = os.path.join(UPLOAD_DIR, orig_filename)
        with open(upload_path, "w", encoding="utf-8") as f:
            f.write(original_text)
        print(f"[✓] Lưu file gốc: {upload_path}")

        # BƯỚC 3: Dịch nội dung

        print(f"[⟳] Đang dịch...")
        translated_text = translate_text(original_text, target_lang)
        print(f"[✓] Dịch xong: {len(translated_text)} ký tự")

        # BƯỚC 4: Lưu file đã dịch + gửi về Client
        # Đặt tên file output: "report_vi.txt", "report_ja.txt", ...
        base_name = os.path.splitext(orig_filename)[0]   # "report"
        out_filename = f"{base_name}_{target_lang}.txt"  # "report_vi.txt"

        translated_path = os.path.join(DOWNLOAD_DIR, out_filename)
        with open(translated_path, "w", encoding="utf-8") as f:
            f.write(translated_text)
        print(f"[✓] Lưu file dịch: {translated_path}")

        # Gửi response JSON trước
        response_header = json.dumps({
            "status"   : "ok",
            "filename" : out_filename,
            "lang"     : target_lang,
            "lang_name": LANG_NAMES.get(target_lang, target_lang),
            "char_count": len(translated_text),
        }).encode("utf-8")
        send_block(conn, response_header)

        # Gửi nội dung file đã dịch
        translated_bytes = translated_text.encode("utf-8")
        send_block(conn, translated_bytes)

        print(f"[←] Đã gửi file '{out_filename}' ({len(translated_bytes)} bytes) về {client_id}")

    except json.JSONDecodeError as e:
        print(f"[!] Lỗi JSON header từ {client_id}: {e}")
        _send_error(conn, "Header JSON không hợp lệ")

    except ValueError as e:
        print(f"[!] Lỗi ngôn ngữ từ {client_id}: {e}")
        _send_error(conn, str(e))

    except Exception as e:
        print(f"[!] Lỗi xử lý client {client_id}: {e}")
        _send_error(conn, f"Server error: {e}")

    finally:
        conn.close()
        print(f"[-] Đóng kết nối: {client_id}")


def _send_error(conn: socket.socket, message: str):
    """Gửi thông báo lỗi về client."""
    try:
        err = json.dumps({"status": "error", "message": message}).encode("utf-8")
        send_block(conn, err)
        # Gửi file rỗng để client không bị treo ở recv
        send_block(conn, b"")
    except Exception:
        pass