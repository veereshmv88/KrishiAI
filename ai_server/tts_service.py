import os
import sys
import re
from typing import Optional
from dotenv import load_dotenv
from huggingface_hub import snapshot_download

load_dotenv()

# Fix DLL path for Windows before importing DhVaani dependencies
ffmpeg_path = r"E:\Dowloads\ffmpeg-9.0.1-full_build-shared\ffmpeg-9.0.1-full_build-shared\bin"
os.environ["PATH"] = ffmpeg_path + os.pathsep + os.environ["PATH"]
if hasattr(os, 'add_dll_directory'):
    os.add_dll_directory(ffmpeg_path)

class TTSService:
    def __init__(self):
        self.tts_model = None
        self.ref_en = os.getenv("DHVAANI_REFERENCE_EN", "reference/reference_en.wav")
        self.ref_kn = os.getenv("DHVAANI_REFERENCE_KN", "reference/reference_kn.wav")
        self.ref_text_en = os.getenv("DHVAANI_REF_TEXT_EN", "ये हिंदी का एक सैंपल है।")
        self.ref_text_kn = os.getenv("DHVAANI_REF_TEXT_KN", "ये हिंदी का एक सैंपल है।")
        self.is_loaded = False

    def load_model(self):
        if self.is_loaded:
            return

        print("Loading DhVaani Standalone TTS Engine...")
        try:
            repo_path = snapshot_download("ARTPARK-IISc/DhVaani-0.5", token=os.getenv("HF_TOKEN"))
            
            # Inject repository path and backend to sys.path so standalone DhVaani can import zipvoice
            if repo_path not in sys.path:
                sys.path.append(repo_path)
            backend_path = os.path.join(repo_path, "_backend")
            if backend_path not in sys.path:
                sys.path.append(backend_path)
            
            from dhvaani import DhVaani
            self.tts_model = DhVaani(model_dir=repo_path)
            self.is_loaded = True
            print("DhVaani TTS Engine loaded successfully.")
        except Exception as e:
            print(f"Failed to load DhVaani TTS Engine: {e}")
            self.is_loaded = False

    def clean_text(self, text: str) -> str:
        """Removes Gemini formatting that should not be spoken."""
        # Remove bold/italic markdown
        text = re.sub(r'[*_]{1,3}', '', text)
        # Remove headers
        text = re.sub(r'#+\s', '', text)
        # Remove URLs
        text = re.sub(r'http[s]?://\S+', '', text)
        # Remove code blocks
        text = re.sub(r'`{1,3}[^`]*`{1,3}', '', text)
        # Clean extra whitespace
        text = re.sub(r'\s+', ' ', text).strip()
        return text

    def synthesize(self, text: str, language: str, out_path: str) -> bool:
        if not self.is_loaded or self.tts_model is None:
            raise RuntimeError("TTS Engine is not loaded.")

        cleaned_text = self.clean_text(text)
        if not cleaned_text:
            raise ValueError("Text is empty after cleaning.")

        if language == "en":
            ref_wav = self.ref_en
            ref_txt = self.ref_text_en
        elif language == "kn":
            ref_wav = self.ref_kn
            ref_txt = self.ref_text_kn
        else:
            raise ValueError(f"Unsupported language: {language}")

        try:
            self.tts_model.synthesize(
                text=cleaned_text,
                prompt_wav=ref_wav,
                prompt_text=ref_txt,
                out_path=out_path
            )
            return True
        except Exception as e:
            print(f"Synthesis failed: {e}")
            return False

# Global instance
tts_service = TTSService()
