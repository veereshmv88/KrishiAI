import os
import torch
import librosa
from transformers import AutoModelForSpeechSeq2Seq, AutoProcessor, pipeline
import logging

logger = logging.getLogger(__name__)

class WhisperTranscriptionResult:
    def __init__(self, text: str, language: str, success: bool, error: str = None):
        self.text = text
        self.language = language
        self.success = success
        self.error = error

class WhisperService:
    def __init__(self):
        self.model_id = "openai/whisper-large-v3"
        self.device = "cuda:0" if torch.cuda.is_available() else "cpu"
        self.torch_dtype = torch.float16 if torch.cuda.is_available() else torch.float32
        self.pipe = None
        self.is_loaded = False
        
    def load_model(self):
        if self.is_loaded:
            return
            
        logger.info(f"Loading Whisper Large V3...")
        logger.info(f"Device: {'CUDA' if 'cuda' in self.device else 'CPU'}")
        
        try:
            model = AutoModelForSpeechSeq2Seq.from_pretrained(
                self.model_id,
                torch_dtype=self.torch_dtype,
                low_cpu_mem_usage=True,
                use_safetensors=True
            )
            model.to(self.device)

            processor = AutoProcessor.from_pretrained(self.model_id)

            self.pipe = pipeline(
                "automatic-speech-recognition",
                model=model,
                tokenizer=processor.tokenizer,
                feature_extractor=processor.feature_extractor,
                torch_dtype=self.torch_dtype,
                device=0 if torch.cuda.is_available() else -1,
            )
            self.is_loaded = True
            logger.info("Whisper loaded successfully.")
        except Exception as e:
            logger.error(f"Failed to load Whisper model: {str(e)}")
            raise e

    def transcribe(self, audio_path: str, language: str = "auto") -> WhisperTranscriptionResult:
        if not self.is_loaded:
            self.load_model()
            
        # Validate audio file
        if not os.path.exists(audio_path):
            return WhisperTranscriptionResult("", None, False, "Audio file not found")
        
        if os.path.getsize(audio_path) == 0:
            return WhisperTranscriptionResult("", None, False, "Audio file is empty")
            
        try:
            # Ensure it's readable by librosa to catch format errors early
            y, sr = librosa.load(audio_path, sr=16000)
            if len(y) == 0:
                return WhisperTranscriptionResult("", None, False, "Audio stream is empty or corrupted")
        except Exception as e:
            return WhisperTranscriptionResult("", None, False, f"Invalid or unsupported audio format: {str(e)}")

        try:
            logger.info("Transcription started")
            generate_kwargs = {}
            
            if language == "en":
                generate_kwargs["language"] = "english"
                generate_kwargs["task"] = "transcribe"
            elif language == "kn":
                generate_kwargs["language"] = "kannada"
                generate_kwargs["task"] = "transcribe"
            
            # If "auto", we don't set language explicitly and just use transcribe task
            if language == "auto":
                generate_kwargs["task"] = "transcribe"

            result = self.pipe(
                audio_path,
                generate_kwargs=generate_kwargs,
                return_timestamps=False
            )
            
            logger.info("Transcription completed")
            return WhisperTranscriptionResult(
                text=result["text"].strip(),
                language=language if language in ["en", "kn"] else "auto",
                success=True
            )
            
        except Exception as e:
            logger.error(f"Transcription failed: {str(e)}")
            return WhisperTranscriptionResult("", None, False, f"Transcription error: {str(e)}")
