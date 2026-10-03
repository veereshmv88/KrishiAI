package com.krishiai.app.data.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.krishiai.app.domain.ai.SpeechToTextEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSpeechToTextEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechToTextEngine, RecognitionListener {

    private val _state = MutableStateFlow(SpeechToTextEngine.SpeechState.IDLE)
    override val state: StateFlow<SpeechToTextEngine.SpeechState> = _state.asStateFlow()
    
    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _recognitionResult = MutableStateFlow<com.krishiai.app.domain.ai.SpeechRecognitionResult?>(null)
    override val recognitionResult: StateFlow<com.krishiai.app.domain.ai.SpeechRecognitionResult?> = _recognitionResult.asStateFlow()

    override fun startListening(language: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechToTextEngine.SpeechState.ERROR
            return
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)
            }
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == "kn") "kn-IN" else "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            _state.value = SpeechToTextEngine.SpeechState.LISTENING
            _recognitionResult.value = null
            speechRecognizer?.startListening(intent)
        }
    }

    override fun stopListening() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            speechRecognizer?.stopListening()
            _state.value = SpeechToTextEngine.SpeechState.IDLE
        }
    }

    override fun cancel() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            speechRecognizer?.cancel()
            _state.value = SpeechToTextEngine.SpeechState.IDLE
        }
    }
    
    fun destroy() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        _state.value = SpeechToTextEngine.SpeechState.ERROR
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _recognitionResult.value = com.krishiai.app.domain.ai.SpeechRecognitionResult(text = matches[0], isFinal = true)
            _state.value = SpeechToTextEngine.SpeechState.SUCCESS
        } else {
            _state.value = SpeechToTextEngine.SpeechState.ERROR
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _recognitionResult.value = com.krishiai.app.domain.ai.SpeechRecognitionResult(text = matches[0], isFinal = false)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
