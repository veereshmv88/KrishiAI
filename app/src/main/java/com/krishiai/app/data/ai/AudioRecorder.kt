package com.krishiai.app.data.ai

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    suspend fun startRecording(): ByteArray = withContext(Dispatchers.IO) {
        if (isRecording) {
            throw IllegalStateException("Already recording")
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            throw IllegalStateException("AudioRecord failed to initialize")
        }

        audioRecord?.startRecording()
        isRecording = true

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(bufferSize)
        var totalBytesRead = 0

        try {
            while (isActive && isRecording) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                }
                
                // Maximum 60 seconds (16000 samples/sec * 2 bytes/sample = 32000 bytes/sec)
                if (totalBytesRead > 32000 * 60) {
                    break
                }
            }
        } finally {
            stopInternal()
        }

        // Minimum 0.5 seconds of audio (32000 * 0.5 = 16000 bytes)
        val pcmData = outputStream.toByteArray()
        if (pcmData.size < 16000) {
            throw IllegalArgumentException("Recording too short")
        }

        return@withContext pcmData
    }

    fun stopRecording() {
        isRecording = false
    }

    private fun stopInternal() {
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            // Ignore stop errors
        } finally {
            audioRecord?.release()
            audioRecord = null
            isRecording = false
        }
    }
}
