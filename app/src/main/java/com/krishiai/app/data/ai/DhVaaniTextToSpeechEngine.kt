package com.krishiai.app.data.ai

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.krishiai.app.data.remote.dhvaani.DhVaaniTtsRepository
import com.krishiai.app.domain.ai.TextToSpeechEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DhVaaniTextToSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DhVaaniTtsRepository,
    private val fallbackEngine: AndroidTextToSpeechEngine
) : TextToSpeechEngine {

    private val _state = MutableStateFlow(TextToSpeechEngine.TtsState.IDLE)
    override val state: StateFlow<TextToSpeechEngine.TtsState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var job: Job? = null
    private var currentFile: File? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun speak(text: String, language: String) {
        stop()
        _state.value = TextToSpeechEngine.TtsState.LOADING

        job = scope.launch {
            val chunks = chunkText(text)
            var allSuccessful = true
            
            for (i in chunks.indices) {
                val chunk = chunks[i].trim()
                if (chunk.isEmpty()) continue
                
                if (!isActive) break
                
                val result = withContext(Dispatchers.IO) {
                    repository.synthesize(chunk, language)
                }

                if (result.isSuccess && isActive) {
                    val uri = result.getOrNull()
                    if (uri != null) {
                        playAudioSync(uri)
                    } else {
                        allSuccessful = false
                        break
                    }
                } else {
                    allSuccessful = false
                    // Handle fallback
                    val remainingText = chunks.subList(i, chunks.size).joinToString(" ")
                    fallbackToAndroidTts(remainingText, language)
                    break
                }
            }

            if (isActive && allSuccessful) {
                _state.value = TextToSpeechEngine.TtsState.COMPLETED
            }
        }
    }

    private suspend fun playAudioSync(uri: Uri) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnPreparedListener {
                    _state.value = TextToSpeechEngine.TtsState.SPEAKING
                    start()
                }
                setOnCompletionListener {
                    cleanupCurrentMedia(uri)
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.success(Unit))
                    }
                }
                setOnErrorListener { _, _, _ ->
                    cleanupCurrentMedia(uri)
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.failure(Exception("MediaPlayer error")))
                    }
                    true
                }
                prepareAsync()
            }
            
            continuation.invokeOnCancellation {
                cleanupCurrentMedia(uri)
            }
        } catch (e: Exception) {
            cleanupCurrentMedia(uri)
            if (continuation.isActive) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    private fun cleanupCurrentMedia(uri: Uri? = null) {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            
            uri?.path?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun chunkText(text: String): List<String> {
        // Split by newlines or sentence-ending punctuation, keeping boundaries intact
        val regex = Regex("(?<=[.!?।\n])\\s+")
        return text.split(regex).filter { it.isNotBlank() }
    }

    private fun fallbackToAndroidTts(text: String, language: String) {
        cleanupCurrentMedia()
        fallbackEngine.speak(text, language)
        
        // Forward fallback state
        scope.launch {
            fallbackEngine.state.collect { fallbackState ->
                if (isActive) {
                    _state.value = fallbackState
                }
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
        cleanupCurrentMedia()
        fallbackEngine.stop()
        _state.value = TextToSpeechEngine.TtsState.IDLE
    }

    override fun pause() {
        mediaPlayer?.pause()
        fallbackEngine.pause()
    }

    override fun resume() {
        mediaPlayer?.start()
        fallbackEngine.resume()
    }
}
