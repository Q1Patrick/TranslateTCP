# System Architecture

## Overview

Hệ thống gồm 2 thành phần:

1. Client (Java)
2. Server (Python)

---

## Flow hoạt động

Client:
- Chọn file
- Gửi file qua TCP

Server:
- Nhận file
- Dịch bằng AI (GoogleTranslator)
- Trả kết quả

---

## Sơ đồ

Client (Java)
   ↓ TCP
Server (Python)
   ↓
AI Translation
   ↓
Return Result

---

## Công nghệ

- Python (Socket, AI)
- Java (TCP Client)
- UTF-8 Encoding