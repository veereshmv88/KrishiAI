import os
import uuid
import logging
from fastapi import FastAPI, UploadFile, Form, HTTPException, BackgroundTasks
from pydantic import BaseModel
from fastapi.responses import JSONResponse, FileResponse
import torch

from whisper_service import WhisperService
from tts_service import tts_service

# Setup logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

app = FastAPI(title="KrishiAI Voice Service")
whisper_service = WhisperService()

class TTSRequest(BaseModel):
    text: str
    language: str

@app.on_event("startup")
async def startup_event():
    logger.info("Server starting up...")
    whisper_service.load_model()
    # Load DhVaani model once on startup
    tts_service.load_model()
    logger.info("Server ready.")

@app.get("/health")
async def health_check():
    return {
        "status": "ok",
        "whisper_model": whisper_service.model_id,
        "tts_loaded": tts_service.is_loaded,
        "device": "cuda" if torch.cuda.is_available() else "cpu"
    }

@app.get("/model-status")
async def model_status():
    return {
        "whisperLoaded": whisper_service.is_loaded,
        "ttsLoaded": tts_service.is_loaded,
        "device": "cuda" if torch.cuda.is_available() else "cpu"
    }

def cleanup_file(filepath: str):
    try:
        if os.path.exists(filepath):
            os.remove(filepath)
            logger.info(f"Deleted temporary file: {filepath}")
    except Exception as e:
        logger.error(f"Failed to delete temporary file {filepath}: {str(e)}")

@app.post("/transcribe")
async def transcribe(background_tasks: BackgroundTasks, audio: UploadFile, language: str = Form("auto")):
    logger.info(f"Request received. Language: {language}")
    
    if language not in ["auto", "en", "kn"]:
        language = "auto"
        
    if not audio:
        return JSONResponse(status_code=400, content={
            "success": False,
            "text": "",
            "language": None,
            "error": "No audio file provided"
        })
        
    temp_filepath = f"temp_{uuid.uuid4()}_{audio.filename}"
    
    try:
        content = await audio.read()
        
        if len(content) == 0:
            return JSONResponse(status_code=400, content={
                "success": False,
                "text": "",
                "language": None,
                "error": "Zero-byte upload"
            })
            
        with open(temp_filepath, "wb") as f:
            f.write(content)
            
        logger.info(f"Audio validated and saved to {temp_filepath}")
        
        background_tasks.add_task(cleanup_file, temp_filepath)
        
        result = whisper_service.transcribe(temp_filepath, language=language)
        
        if result.success:
            return {
                "success": True,
                "text": result.text,
                "language": result.language
            }
        else:
            return JSONResponse(status_code=400, content={
                "success": False,
                "text": "",
                "language": None,
                "error": result.error
            })
            
    except Exception as e:
        logger.error(f"Error during request processing: {str(e)}")
        cleanup_file(temp_filepath)
        return JSONResponse(status_code=500, content={
            "success": False,
            "text": "",
            "language": None,
            "error": "Internal server error"
        })

@app.post("/tts")
async def synthesize_tts(request: TTSRequest, background_tasks: BackgroundTasks):
    logger.info(f"TTS Request received. Language: {request.language}, Text length: {len(request.text)}")
    
    if request.language not in ["en", "kn"]:
        raise HTTPException(status_code=400, detail="Unsupported language. Use 'en' or 'kn'.")
        
    if not request.text or len(request.text.strip()) == 0:
        raise HTTPException(status_code=400, detail="Text cannot be empty.")
        
    if not tts_service.is_loaded:
        raise HTTPException(status_code=503, detail="TTS Engine is not loaded.")
        
    temp_out = f"tts_out_{uuid.uuid4()}.wav"
    
    try:
        success = tts_service.synthesize(
            text=request.text, 
            language=request.language,
            out_path=temp_out
        )
        
        if not success or not os.path.exists(temp_out):
            raise HTTPException(status_code=500, detail="Synthesis failed.")
            
        # Add background task to clean up the WAV file after sending
        background_tasks.add_task(cleanup_file, temp_out)
        
        return FileResponse(
            path=temp_out,
            media_type="audio/wav",
            filename=f"dhvaani_{request.language}.wav"
        )
        
    except Exception as e:
        logger.error(f"Error during TTS synthesis: {str(e)}")
        cleanup_file(temp_out)
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
