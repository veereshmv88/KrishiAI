package com.krishiai.app.data.ai

import android.util.Log
import com.krishiai.app.domain.ai.SpeechRecognitionResult
import com.krishiai.app.domain.ai.SpeechToTextEngine
import com.krishiai.app.domain.repository.WhisperRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhisperSpeechToTextEngine @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val whisperRepository: WhisperRepository,
    private val fallbackEngine: AndroidSpeechToTextEngine
) : SpeechToTextEngine {

    private val _state = MutableStateFlow(SpeechToTextEngine.SpeechState.IDLE)
    override val state: StateFlow<SpeechToTextEngine.SpeechState> = _state.asStateFlow()
    
    // We also expose the result from fallback or whisper
    override val recognitionResult: StateFlow<SpeechRecognitionResult?> 
        get() = _recognitionResult.asStateFlow()
        
    private val _recognitionResult = MutableStateFlow<SpeechRecognitionResult?>(null)

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var recordingJob: Job? = null
    private var processingJob: Job? = null
    private var isFallbackActive = false
    private var currentLanguage = "en"

    init {
        // Forward state from fallback if active
        scope.launch {
            fallbackEngine.state.collect { fallbackState ->
                if (isFallbackActive) {
                    _state.value = fallbackState
                }
            }
        }
    }

    override fun startListening(language: String) {
        cancel() // Clear previous states
        
        currentLanguage = language
        isFallbackActive = false
        _recognitionResult.value = null
        _state.value = SpeechToTextEngine.SpeechState.LISTENING

        recordingJob = scope.launch(Dispatchers.IO) {
            try {
                // If it fails here (e.g. no permission), exception is thrown
                val pcmData = audioRecorder.startRecording()
                
                // If we get here automatically (e.g., 60s max reached)
                // We should start processing if it hasn't been cancelled
                processAudio(pcmData)
                
            } catch (e: IllegalArgumentException) {
                // Recording too short
                Log.d("WhisperSTT", "Recording too short: ${e.message}")
                _state.value = SpeechToTextEngine.SpeechState.ERROR
            } catch (e: Exception) {
                Log.e("WhisperSTT", "Recording failed", e)
                _state.value = SpeechToTextEngine.SpeechState.ERROR
            }
        }
    }

    override fun stopListening() {
        if (isFallbackActive) {
            fallbackEngine.stopListening()
            return
        }

        if (_state.value == SpeechToTextEngine.SpeechState.LISTENING) {
            audioRecorder.stopRecording()
            // The processAudio will be called from the startListening coroutine when stopRecording() returns
        }
    }

    override fun cancel() {
        recordingJob?.cancel()
        processingJob?.cancel()
        audioRecorder.stopRecording()
        
        if (isFallbackActive) {
            fallbackEngine.cancel()
        }
        
        isFallbackActive = false
        _state.value = SpeechToTextEngine.SpeechState.IDLE
    }
    
    private fun processAudio(pcmData: ByteArray) {
        _state.value = SpeechToTextEngine.SpeechState.PROCESSING
        
        processingJob = scope.launch(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                Log.d("WhisperSTT", "Uploading ${pcmData.size} bytes for language $currentLanguage...")
                
                val result = whisperRepository.transcribeAudio(pcmData, currentLanguage)
                
                val latency = System.currentTimeMillis() - start
                Log.d("WhisperSTT", "Whisper processing took $latency ms")
                
                if (result.isSuccess) {
                    _recognitionResult.value = result.getOrThrow()
                    _state.value = SpeechToTextEngine.SpeechState.SUCCESS
                } else {
                    val ex = result.exceptionOrNull()
                    Log.e("WhisperSTT", "Whisper transcription failed", ex)
                    triggerFallback()
                }
            } catch (e: Exception) {
                Log.e("WhisperSTT", "Failed to process audio", e)
                triggerFallback()
            }
        }
    }
    
    private fun triggerFallback() {
        Log.w("WhisperSTT", "Whisper unavailable — using device speech recognition.")
        isFallbackActive = true
        
        scope.launch(Dispatchers.Main) {
            fallbackEngine.startListening(currentLanguage)
        }
    }
}
