import os
import sys
import platform
import psutil
import torch
import torchaudio
import transformers
import accelerate
import safetensors
import huggingface_hub

print("=== PHASE A: ENVIRONMENT INSPECTION ===")
print(f"Python: {sys.version}")
print(f"torch: {torch.__version__}")
print(f"torchaudio: {torchaudio.__version__}")
print(f"transformers: {transformers.__version__}")
print(f"accelerate: {accelerate.__version__}")
print(f"safetensors: {safetensors.__version__}")
print(f"huggingface_hub: {huggingface_hub.__version__}")

print(f"torch.cuda.is_available(): {torch.cuda.is_available()}")
print(f"CPU architecture: {platform.machine()} {platform.processor()}")
print(f"Available system RAM: {psutil.virtual_memory().available / (1024**3):.2f} GB / {psutil.virtual_memory().total / (1024**3):.2f} GB")

print("\n=== PHASE B & E: INSPECT DHVAANI IMPLEMENTATION (META TENSORS) ===")
from transformers import AutoModel
from dotenv import load_dotenv

load_dotenv()
HF_TOKEN = os.getenv("HF_TOKEN")

# Fix DLL path
ffmpeg_path = r"E:\Dowloads\ffmpeg-9.0.1-full_build-shared\ffmpeg-9.0.1-full_build-shared\bin"
os.environ["PATH"] = ffmpeg_path + os.pathsep + os.environ["PATH"]
if hasattr(os, 'add_dll_directory'):
    os.add_dll_directory(ffmpeg_path)

try:
    print("Loading model without device_map or low_cpu_mem_usage to check default behavior...")
    model = AutoModel.from_pretrained("ARTPARK-IISc/DhVaani-0.5", trust_remote_code=True, token=HF_TOKEN)
    
    meta_params = []
    for name, param in model.named_parameters():
        if param.device.type == "meta":
            meta_params.append((name, param.shape))
            
    meta_buffers = []
    for name, buf in model.named_buffers():
        if buf.device.type == "meta":
            meta_buffers.append((name, buf.shape))
            
    print(f"Found {len(meta_params)} meta parameters and {len(meta_buffers)} meta buffers.")
    if meta_params:
        print("First 5 meta params:", meta_params[:5])
    if meta_buffers:
        print("First 5 meta buffers:", meta_buffers[:5])
        
except Exception as e:
    print(f"Error loading model: {e}")
