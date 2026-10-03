package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.data.local.dao.ChatHistoryDao
import com.krishiai.app.data.local.entity.ChatHistoryEntity
import com.krishiai.app.data.local.prefs.DataStoreManager
import com.krishiai.app.domain.ai.ChatMessage
import com.krishiai.app.domain.ai.IAIVoiceAssistant
import com.krishiai.app.domain.ai.VoiceAssistantState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.krishiai.app.domain.ai.SpeechToTextEngine
import com.krishiai.app.domain.ai.SpeechRecognitionResult

@HiltViewModel
class VoiceAssistantViewModel @Inject constructor(
    private val aiVoiceAssistant: IAIVoiceAssistant,
    private val dataStoreManager: DataStoreManager,
    private val chatHistoryDao: ChatHistoryDao,
    private val languageManager: com.krishiai.app.core.language.LanguageManager,
    private val speechToTextEngine: SpeechToTextEngine
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Map SpeechToTextEngine state to VoiceAssistantState for UI, and combine with AIVoiceAssistant state (for SPEAKING/PROCESSING)
    private val _uiState = MutableStateFlow(VoiceAssistantState.IDLE)
    val aiState: StateFlow<VoiceAssistantState> = _uiState.asStateFlow()
    
    // We don't have partial text with Whisper, so we keep it empty or mock it if needed
    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    val currentLanguage: StateFlow<String> = languageManager.currentLanguage

    init {
        loadChatHistory()
        viewModelScope.launch {
            // Listen for AI Speaking states
            launch {
                aiVoiceAssistant.state.collect { state ->
                    if (state == VoiceAssistantState.SPEAKING || state == VoiceAssistantState.PROCESSING) {
                        _uiState.value = state
                    } else if (state == VoiceAssistantState.IDLE && _uiState.value == VoiceAssistantState.SPEAKING) {
                        _uiState.value = VoiceAssistantState.IDLE
                    }
                }
            }
            
            // Listen for STT Engine states
            launch {
                speechToTextEngine.state.collect { sttState ->
                    when (sttState) {
                        SpeechToTextEngine.SpeechState.IDLE -> {
                            if (_uiState.value == VoiceAssistantState.LISTENING || _uiState.value == VoiceAssistantState.ERROR) {
                                _uiState.value = VoiceAssistantState.IDLE
                            }
                        }
                        SpeechToTextEngine.SpeechState.LISTENING -> _uiState.value = VoiceAssistantState.LISTENING
                        SpeechToTextEngine.SpeechState.PROCESSING -> _uiState.value = VoiceAssistantState.PROCESSING
                        SpeechToTextEngine.SpeechState.ERROR -> _uiState.value = VoiceAssistantState.ERROR
                        SpeechToTextEngine.SpeechState.SUCCESS -> {
                            // UI processing state continues while Gemini processes
                        }
                    }
                }
            }
            
            // Listen for STT Success results
            launch {
                if (speechToTextEngine is com.krishiai.app.data.ai.WhisperSpeechToTextEngine) {
                    speechToTextEngine.recognitionResult.collect { result ->
                        if (result != null && result.text.isNotBlank()) {
                            // Reset the recognition result so it only fires once per successful speech
                            sendQuery(result.text)
                        }
                    }
                } else if (speechToTextEngine is com.krishiai.app.data.ai.AndroidSpeechToTextEngine) {
                    speechToTextEngine.recognitionResult.collect { result ->
                        if (result != null && result.isFinal && result.text.isNotBlank()) {
                            sendQuery(result.text)
                        }
                    }
                }
            }
            
            // Listen for fallback partial results if using AndroidSpeechToTextEngine
            launch {
                 if (speechToTextEngine is com.krishiai.app.data.ai.AndroidSpeechToTextEngine) {
                     speechToTextEngine.recognitionResult.collect { result ->
                         if (result != null && !result.isFinal) {
                             _partialText.value = result.text
                         } else if (result == null) {
                             _partialText.value = ""
                         }
                     }
                 }
            }
        }
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            chatHistoryDao.getChatHistory().collect { entities ->
                _messages.value = entities.map {
                    ChatMessage(
                        id = it.messageId,
                        text = it.content,
                        isFromUser = it.role == "user",
                        timestamp = it.timestamp
                    )
                }
            }
        }
    }

    fun setLanguage(langCode: String) {
        languageManager.setLanguage(langCode)
    }

    fun toggleListening() {
        if (_uiState.value == VoiceAssistantState.LISTENING) {
            speechToTextEngine.stopListening()
        } else if (_uiState.value == VoiceAssistantState.SPEAKING) {
            aiVoiceAssistant.stopSpeaking()
        } else {
            speechToTextEngine.startListening(currentLanguage.value)
        }
    }

    fun sendQuery(query: String) {
        _partialText.value = ""
        addUserMessage(query)
        processQuery(query)
    }

    private fun processQuery(query: String) {
        viewModelScope.launch {
            _uiState.value = VoiceAssistantState.PROCESSING
            val response = aiVoiceAssistant.processQuery(query, currentLanguage.value)
            addAIMessage(response)
            aiVoiceAssistant.speak(response, currentLanguage.value)
        }
    }

    fun repeatLastAnswer() {
        val lastAI = _messages.value.lastOrNull { !it.isFromUser }
        lastAI?.let {
            aiVoiceAssistant.speak(it.text, currentLanguage.value)
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            chatHistoryDao.clearHistory()
            aiVoiceAssistant.stopSpeaking()
            speechToTextEngine.cancel()
        }
    }

    private fun addUserMessage(text: String) {
        viewModelScope.launch {
            val entity = ChatHistoryEntity(
                messageId = UUID.randomUUID().toString(),
                role = "user",
                content = text,
                timestamp = System.currentTimeMillis(),
                language = currentLanguage.value
            )
            chatHistoryDao.insertMessage(entity)
        }
    }

    private fun addAIMessage(text: String) {
        viewModelScope.launch {
            val entity = ChatHistoryEntity(
                messageId = UUID.randomUUID().toString(),
                role = "model",
                content = text,
                timestamp = System.currentTimeMillis(),
                language = currentLanguage.value
            )
            chatHistoryDao.insertMessage(entity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiVoiceAssistant.destroy()
    }
}
