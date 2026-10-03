import os
import sys
import time
from dotenv import load_dotenv
from huggingface_hub import snapshot_download
import soundfile as sf

print("=== PHASE C: TEST THE OFFICIAL STANDALONE API ===")
load_dotenv()
HF_TOKEN = os.getenv("HF_TOKEN")

# Fix DLL path
ffmpeg_path = r"E:\Dowloads\ffmpeg-9.0.1-full_build-shared\ffmpeg-9.0.1-full_build-shared\bin"
os.environ["PATH"] = ffmpeg_path + os.pathsep + os.environ["PATH"]
if hasattr(os, 'add_dll_directory'):
    os.add_dll_directory(ffmpeg_path)

try:
    print("Downloading/Locating model repository...")
    repo_path = snapshot_download("ARTPARK-IISc/DhVaani-0.5", token=HF_TOKEN)
    sys.path.append(repo_path)
    sys.path.append(os.path.join(repo_path, "_backend"))
    
    print("Importing DhVaani from standalone API...")
    from dhvaani import DhVaani
    
    print("Initializing standalone DhVaani()...")
    start = time.time()
    tts = DhVaani(model_dir=repo_path)
    load_time = time.time() - start
    print(f"Standalone DhVaani loaded in {load_time:.2f}s")
    
    print("Testing standalone English synthesis...")
    en_start = time.time()
    tts.synthesize(
        text="Hello, welcome to KrishiAI. I can help you with farming, weather and market information.",
        prompt_wav="reference/reference_en.wav",
        prompt_text="ये हिंदी का एक सैंपल है।",
        out_path="standalone_english.wav"
    )
    en_time = time.time() - en_start
    print(f"English standalone synthesis SUCCESS in {en_time:.2f}s")
    
    print("Testing standalone Kannada synthesis...")
    kn_start = time.time()
    tts.synthesize(
        text="ನಮಸ್ಕಾರ, ಕ್ರಿಷಿಏಐಗೆ ಸ್ವಾಗತ. ನಾನು ಕೃಷಿ, ಹವಾಮಾನ ಮತ್ತು ಮಾರುಕಟ್ಟೆ ಮಾಹಿತಿಯಲ್ಲಿ ನಿಮಗೆ ಸಹಾಯ ಮಾಡಬಹುದು.",
        prompt_wav="reference/reference_kn.wav",
        prompt_text="ये हिंदी का एक सैंपल है。",
        out_path="standalone_kannada.wav"
    )
    kn_time = time.time() - kn_start
    print(f"Kannada standalone synthesis SUCCESS in {kn_time:.2f}s")
    
except Exception as e:
    import traceback
    print(f"Error testing standalone API:")
    traceback.print_exc()
