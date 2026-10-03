import os
import sys
import traceback
import torch
from transformers import AutoModel
from dotenv import load_dotenv

load_dotenv()
HF_TOKEN = os.getenv("HF_TOKEN")

ffmpeg_path = r"E:\Dowloads\ffmpeg-9.0.1-full_build-shared\ffmpeg-9.0.1-full_build-shared\bin"
os.environ["PATH"] = ffmpeg_path + os.pathsep + os.environ["PATH"]
if hasattr(os, 'add_dll_directory'):
    os.add_dll_directory(ffmpeg_path)

print("Loading model...")
model = AutoModel.from_pretrained("ARTPARK-IISc/DhVaani-0.5", trust_remote_code=True, token=HF_TOKEN, low_cpu_mem_usage=False)

print("Running synthesize to capture traceback...")
try:
    with open("reference/reference_en.txt", "r", encoding="utf-8") as f:
        en_ref_text = f.read().strip()
    en_audio = model.synthesize(text="test", prompt_wav="reference/reference_en.wav", prompt_text=en_ref_text)
except Exception as e:
    traceback.print_exc()
