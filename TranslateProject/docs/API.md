# TCP Protocol Specification

## Connection
- Host: 127.0.0.1
- Port: 9999
- Protocol: TCP

---

## Request (Client → Server)

1. JSON Header:
{
  "lang": "vi",
  "filename": "example.txt"
}

2. File Content:
- UTF-8 text

---

## Format truyền dữ liệu

[4 bytes length][data]

---

## Response (Server → Client)

1. JSON:
{
  "status": "ok",
  "filename": "example_vi.txt",
  "lang": "vi",
  "char_count": 120
}

2. Nội dung file đã dịch (UTF-8)

---

## Error

{
  "status": "error",
  "message": "..."
}