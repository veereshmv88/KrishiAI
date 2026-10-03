package com.krishiai.app.data.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.krishiai.app.domain.ai.IAIVoiceAssistant
import com.krishiai.app.domain.ai.VoiceAssistantState
import com.krishiai.app.domain.ai.AIReasoningEngine
import com.krishiai.app.domain.ai.TextToSpeechEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIVoiceAssistantImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiReasoningEngine: AIReasoningEngine,
    private val textToSpeechEngine: TextToSpeechEngine
) : IAIVoiceAssistant, RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _state = MutableStateFlow(VoiceAssistantState.IDLE)
    override val state: StateFlow<VoiceAssistantState> = _state.asStateFlow()

    private val _partialText = MutableStateFlow("")
    override val partialText: StateFlow<String> = _partialText.asStateFlow()

    init {
        // Collect TTS states and map to VoiceAssistantState
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            textToSpeechEngine.state.collect { ttsState ->
                when (ttsState) {
                    TextToSpeechEngine.TtsState.SPEAKING -> _state.value = VoiceAssistantState.SPEAKING
                    TextToSpeechEngine.TtsState.COMPLETED -> _state.value = VoiceAssistantState.IDLE
                    TextToSpeechEngine.TtsState.ERROR -> _state.value = VoiceAssistantState.ERROR
                    TextToSpeechEngine.TtsState.IDLE -> {
                        if (_state.value == VoiceAssistantState.SPEAKING || _state.value == VoiceAssistantState.ERROR) {
                            _state.value = VoiceAssistantState.IDLE
                        }
                    }
                    TextToSpeechEngine.TtsState.LOADING -> _state.value = VoiceAssistantState.SPEAKING
                }
            }
        }
    }

    override fun startListening(languageCode: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = VoiceAssistantState.ERROR
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
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (languageCode == "kn") "kn-IN" else "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            _state.value = VoiceAssistantState.LISTENING
            _partialText.value = ""
            speechRecognizer?.startListening(intent)
        }
    }

    override fun stopListening() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            speechRecognizer?.stopListening()
        }
    }

    override fun speak(text: String, languageCode: String) {
        textToSpeechEngine.speak(text, languageCode)
    }

    override fun stopSpeaking() {
        textToSpeechEngine.stop()
        _state.value = VoiceAssistantState.IDLE
    }

    override suspend fun processQuery(query: String, languageCode: String): String {
        _state.value = VoiceAssistantState.PROCESSING
        val response = aiReasoningEngine.processQuery(query, languageCode)
        return response.text
    }

    override fun destroy() {
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
        _state.value = VoiceAssistantState.ERROR
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val finalResult = matches[0]
            _partialText.value = finalResult
            _state.value = VoiceAssistantState.PROCESSING
        } else {
            _state.value = VoiceAssistantState.ERROR
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _partialText.value = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
