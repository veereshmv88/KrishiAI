package com.krishiai.app.data.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.krishiai.app.domain.ai.TextToSpeechEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidTextToSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeechEngine, TextToSpeech.OnInitListener {

    private val _state = MutableStateFlow(TextToSpeechEngine.TtsState.IDLE)
    override val state: StateFlow<TextToSpeechEngine.TtsState> = _state.asStateFlow()
    
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.value = TextToSpeechEngine.TtsState.SPEAKING
                }
                override fun onDone(utteranceId: String?) {
                    _state.value = TextToSpeechEngine.TtsState.COMPLETED
                }
                override fun onError(utteranceId: String?) {
                    _state.value = TextToSpeechEngine.TtsState.ERROR
                }
            })
        }
    }

    override fun speak(text: String, language: String) {
        if (!isTtsInitialized) return
        
        val locale = if (language == "kn") Locale("kn", "IN") else Locale.US
        val result = textToSpeech?.setLanguage(locale)
        
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _state.value = TextToSpeechEngine.TtsState.ERROR
            return
        }
        
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_id_${System.currentTimeMillis()}")
    }

    override fun stop() {
        textToSpeech?.stop()
        _state.value = TextToSpeechEngine.TtsState.IDLE
    }

    override fun pause() {
        // Android TTS does not natively support pause out of the box in this API level without chopping sentences
        stop()
    }

    override fun resume() {
        // Not natively supported without maintaining state
    }
    
    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
