package com.krishiai.app.data.repository

import android.content.Context
import com.krishiai.app.data.ai.WavAudioEncoder
import com.krishiai.app.data.remote.whisper.WhisperApiService
import com.krishiai.app.domain.ai.SpeechRecognitionResult
import com.krishiai.app.domain.repository.WhisperRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhisperRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whisperApiService: WhisperApiService
) : WhisperRepository {

    override suspend fun transcribeAudio(pcmData: ByteArray, language: String): Result<SpeechRecognitionResult> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            // Encode PCM to WAV
            val wavData = WavAudioEncoder.encodeToWav(pcmData)

            // Create temporary file
            tempFile = File(context.cacheDir, "whisper_${UUID.randomUUID()}.wav")
            FileOutputStream(tempFile).use { it.write(wavData) }

            // Prepare multipart request
            val requestFile = tempFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", tempFile.name, requestFile)
            val languagePart = language.toRequestBody("text/plain".toMediaTypeOrNull())

            // Make API call
            val response = whisperApiService.transcribeAudio(audioPart, languagePart)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && !body.text.isNullOrBlank()) {
                    Result.success(SpeechRecognitionResult(text = body.text, language = body.language))
                } else {
                    Result.failure(Exception(body?.error ?: "Transcription failed or empty"))
                }
            } else {
                Result.failure(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // Guarantee cleanup
            tempFile?.let {
                if (it.exists()) {
                    it.delete()
                }
            }
        }
    }
}
