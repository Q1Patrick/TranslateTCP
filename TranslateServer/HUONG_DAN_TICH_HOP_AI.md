# Hướng dẫn tích hợp AI dịch thuật MIỄN PHÍ vào server.py



---



### Bước 1: Cài thư viện
```
pip install deep-translator
```

### Bước 2: Điền vào hàm translate_to_vietnamese() trong server.py

```python
from deep_translator import GoogleTranslator

def translate_to_vietnamese(english_text: str) -> str:
    try:
        # source="en"  : ngôn ngữ nguồn tiếng Anh
        # target="vi"  : ngôn ngữ đích tiếng Việt
        result = GoogleTranslator(source="en", target="vi").translate(english_text)
        return result
    except Exception as e:
        return f"[Lỗi dịch: {e}] {english_text}"
```

### Giới hạn
- Miễn phí, không cần key
- Giới hạn ~5000 ký tự / lần gọi
- Nếu văn bản dài hơn → tự động cắt đoạn:

```python
from deep_translator import GoogleTranslator

def translate_to_vietnamese(english_text: str) -> str:
    try:
        # Cắt thành đoạn 4000 ký tự để không bị giới hạn
        chunks = [english_text[i:i+4000] for i in range(0, len(english_text), 4000)]
        translated_chunks = []
        for chunk in chunks:
            translated = GoogleTranslator(source="en", target="vi").translate(chunk)
            translated_chunks.append(translated)
        return " ".join(translated_chunks)
    except Exception as e:
        return f"[Lỗi dịch: {e}] {english_text}"    
```

---


