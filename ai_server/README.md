# KrishiAI Whisper Service

This is the backend server for KrishiAI's voice assistant, running OpenAI's Whisper Large V3 for Speech-to-Text (STT) inference.

## Prerequisites

- Python 3.10 or 3.11
- CUDA-compatible GPU (Highly Recommended)
- At least 8-10 GB of VRAM/RAM

## Installation

Create and activate a Python virtual environment:

**Windows**:
```bash
cd ai_server
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

**Linux/Mac**:
```bash
cd ai_server
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Verify GPU Availability

Run this command to check if PyTorch detects your GPU:
```bash
python -c "import torch; print('CUDA Available:', torch.cuda.is_available()); print('Device:', torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'CPU')"
```

## Starting the Server

The server loads the `openai/whisper-large-v3` model once upon startup. Depending on your internet connection and hardware, the initial download and startup may take a few minutes.

```bash
# Ensure your virtual environment is active
python -m uvicorn main:app --host 0.0.0.0 --port 8000
```

## Endpoints

### `GET /health`
Returns the server status and hardware device.

### `GET /model-status`
Returns information about the loaded model.

### `POST /transcribe`
Transcribes audio.

**Form Data Parameters:**
- `audio`: The audio file (WAV, MP3, etc.)
- `language` (optional): `auto` (default), `en` (English), or `kn` (Kannada)

## Manual Testing (cURL)

**English:**
```bash
curl -X POST "http://127.0.0.1:8000/transcribe" -F "audio=@english.wav" -F "language=en"
```

**Kannada:**
```bash
curl -X POST "http://127.0.0.1:8000/transcribe" -F "audio=@kannada.wav" -F "language=kn"
```

**Auto / Mixed:**
```bash
curl -X POST "http://127.0.0.1:8000/transcribe" -F "audio=@mixed.wav" -F "language=auto"
```

## Troubleshooting

- **Out of Memory (OOM):** Whisper Large V3 requires significant memory. If you get CUDA out of memory errors, close other applications using GPU memory.
- **Slow Inference:** If it's running on CPU (check `/health`), transcription will be significantly slower than on a GPU. Ensure CUDA toolkit is properly installed.
- **Audio Validation Errors:** Ensure the uploaded audio is a valid format supported by `soundfile` / `librosa`.
