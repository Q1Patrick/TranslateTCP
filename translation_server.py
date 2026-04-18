import socket
import threading
import google.generativeai as genai

# 1. Cấu hình Gemini API
API_KEY = "DÁN_API_KEY_CỦA_BẠN_VÀO_ĐÂY"
genai.configure(api_key=API_KEY)
model = genai.GenerativeModel('gemini-1.5-flash')

def get_translation(text):
    try:
        # Prompt được thiết kế chuyên biệt cho kịch bản/hội thoại
        prompt = f"""Bạn là một dịch giả chuyên nghiệp, đặc biệt am hiểu việc dịch các kịch bản game/Visual Novel (ví dụ: định dạng .rpy).
Nhiệm vụ của bạn là dịch đoạn văn bản sau sang tiếng Việt với các yêu cầu:
1. BẢO TOÀN CẤU TRÚC: Tuyệt đối giữ nguyên các thẻ code, ký tự điều khiển, hoặc tên biến. Chỉ dịch phần nội dung hội thoại/hiển thị.
2. ĐẠI TỪ NHÂN XƯNG: Xử lý khéo léo, tự nhiên và nhất quán hệ thống xưng hô tiếng Việt (anh, em, cậu, tớ, ngài...) dựa trên ngữ cảnh và sắc thái câu thoại.

Văn bản gốc cần dịch:
{text}"""
        response = model.generate_content(prompt)
        return response.text
    except Exception as e:
        return f"[Lỗi AI]: {str(e)}"

# Hàm nhận đủ dữ liệu dựa trên chiều dài (chống phân mảnh TCP)
def recv_all(sock, n):
    data = bytearray()
    while len(data) < n:
        packet = sock.recv(n - len(data))
        if not packet:
            return None
        data.extend(packet)
    return data

# Hàm xử lý từng Client độc lập
def handle_client(client_socket, addr):
    print(f"[+] Đã kết nối luồng mới cho: {addr}")
    try:
        # 1. Nhận 4 byte đầu tiên để biết chiều dài chuỗi text
        raw_msglen = recv_all(client_socket, 4)
        if not raw_msglen:
            return
        
        # Giải mã chiều dài (Big Endian)
        msglen = int.from_bytes(raw_msglen, byteorder='big')
        
        # 2. Nhận chính xác số byte nội dung
        data = recv_all(client_socket, msglen)
        if data:
            text_received = data.decode('utf-8')
            print(f"[{addr}] Yêu cầu dịch ({msglen} bytes)")
            
            # 3. Gọi AI dịch
            translated_text = get_translation(text_received)
            
            # 4. Đóng gói kết quả gửi về (Đính kèm chiều dài trước)
            payload = translated_text.encode('utf-8')
            payload_length = len(payload).to_bytes(4, byteorder='big')
            
            client_socket.sendall(payload_length + payload)
            print(f"[{addr}] Đã phản hồi thành công!")
            
    except Exception as e:
        print(f"[-] Lỗi tại client {addr}: {e}")
    finally:
        client_socket.close()
        print(f"[-] Đã đóng kết nối với {addr}")

# 2. Thiết lập TCP Server Đa luồng
HOST = '127.0.0.1'
PORT = 8888

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((HOST, PORT))
server_socket.listen(5)

print(f"🚀 Server ĐA LUỒNG đang lắng nghe tại {HOST}:{PORT}...")

while True:
    client_sock, address = server_socket.accept()
    # Tạo Thread mới cho mỗi Client
    client_thread = threading.Thread(target=handle_client, args=(client_sock, address))
    client_thread.start()