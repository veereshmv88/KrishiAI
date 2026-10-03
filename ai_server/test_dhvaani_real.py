import os
import time
import soundfile as sf
import torch
from dotenv import load_dotenv
from transformers import AutoModel
import datasets

# Add the user's specific FFmpeg path to the system PATH so torchcodec can find the DLLs
ffmpeg_path = r"E:\Dowloads\ffmpeg-9.0.1-full_build-shared\ffmpeg-9.0.1-full_build-shared\bin"
os.environ["PATH"] = ffmpeg_path + os.pathsep + os.environ["PATH"]
if hasattr(os, 'add_dll_directory'):
    os.add_dll_directory(ffmpeg_path)

load_dotenv()
HF_TOKEN = os.getenv("HF_TOKEN")

if not HF_TOKEN:
    print("HF_TOKEN not found!")
    exit(1)

os.makedirs("reference", exist_ok=True)

import shutil
from huggingface_hub import hf_hub_download

print("Downloading reference voices...")
os.makedirs("reference", exist_ok=True)
try:
    if not os.path.exists("reference/reference_en.wav"):
        print("Downloading hindi.wav from repo as reference...")
        sample_wav = hf_hub_download(repo_id="ARTPARK-IISc/DhVaani-0.5", filename="samples/hindi.wav", token=HF_TOKEN)
        shutil.copy(sample_wav, "reference/reference_en.wav")
        shutil.copy(sample_wav, "reference/reference_kn.wav")
        with open("reference/reference_en.txt", "w", encoding="utf-8") as f:
            f.write("ये हिंदी का एक सैंपल है।")
        with open("reference/reference_kn.txt", "w", encoding="utf-8") as f:
            f.write("ये हिंदी का एक सैंपल है।")
        print("Created reference WAVs from repo sample.")
except Exception as e:
    print(f"Error downloading reference: {e}")

print("Loading DhVaani model...")
start = time.time()
try:
    model = AutoModel.from_pretrained("ARTPARK-IISc/DhVaani-0.5", trust_remote_code=True, token=HF_TOKEN, device_map="cpu", torch_dtype=torch.float32)
    device = "cpu"
    if device == "cuda":
        model = model.to(device)
    load_time = time.time() - start
    print(f"Model loaded successfully on {device} in {load_time:.2f}s")
except Exception as e:
    print(f"Error loading model: {e}")
    exit(1)

# English Test
en_text = "For tomato crops, use fertilizer according to soil requirements and avoid excessive nitrogen."
try:
    with open("reference/reference_en.txt", "r", encoding="utf-8") as f:
        en_ref_text = f.read().strip()
    print("Testing English Synthesis...")
    en_start = time.time()
    en_audio = model.synthesize(text=en_text, prompt_wav="reference/reference_en.wav", prompt_text=en_ref_text)
    sf.write("test_english.wav", en_audio, model.sampling_rate)
    en_time = time.time() - en_start
    print(f"English synthesis SUCCESS in {en_time:.2f}s")
except Exception as e:
    print(f"English synthesis failed: {e}")

# Kannada Test
kn_text = "ಟೊಮೇಟೊ ಬೆಳೆಗೆ ಮಣ್ಣಿನ ಪರೀಕ್ಷೆಯ ಆಧಾರದ ಮೇಲೆ ಗೊಬ್ಬರವನ್ನು ಬಳಸಬೇಕು."
try:
    with open("reference/reference_kn.txt", "r", encoding="utf-8") as f:
        kn_ref_text = f.read().strip()
    print("Testing Kannada Synthesis...")
    kn_start = time.time()
    kn_audio = model.synthesize(text=kn_text, prompt_wav="reference/reference_kn.wav", prompt_text=kn_ref_text)
    sf.write("test_kannada.wav", kn_audio, model.sampling_rate)
    kn_time = time.time() - kn_start
    print(f"Kannada synthesis SUCCESS in {kn_time:.2f}s")
except Exception as e:
    print(f"Kannada synthesis failed: {e}")
