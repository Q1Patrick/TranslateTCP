from deep_translator import GoogleTranslator
from config import LANG_NAMES

def translate_text(text: str, target_lang: str) -> str:
    if target_lang not in LANG_NAMES:
        raise ValueError(f"Ngôn ngữ không hỗ trợ: '{target_lang}'. "
                         f"Chọn trong: {list(LANG_NAMES.keys())}")

    chunks = [text[i:i+4000] for i in range(0, len(text), 4000)]

    translated_chunks = []
    for i, chunk in enumerate(chunks):
        print(f"    Đang dịch đoạn {i+1}/{len(chunks)}...")
        result = GoogleTranslator(source="auto", target=target_lang).translate(chunk)
        translated_chunks.append(result)

    return "\n".join(translated_chunks)