package com.krishiai.app.data.ai

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

object WavAudioEncoder {

    @Throws(IOException::class)
    fun encodeToWav(pcmData: ByteArray, sampleRate: Int = 16000, channels: Int = 1): ByteArray {
        val out = ByteArrayOutputStream()
        val dataOut = DataOutputStream(out)
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = pcmData.size

        // RIFF chunk descriptor
        dataOut.writeBytes("RIFF")
        dataOut.writeInt(Integer.reverseBytes(36 + dataSize))
        dataOut.writeBytes("WAVE")

        // "fmt " sub-chunk
        dataOut.writeBytes("fmt ")
        dataOut.writeInt(Integer.reverseBytes(16)) // Subchunk1Size (16 for PCM)
        dataOut.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat (1 for PCM)
        dataOut.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt()) // NumChannels
        dataOut.writeInt(Integer.reverseBytes(sampleRate)) // SampleRate
        dataOut.writeInt(Integer.reverseBytes(byteRate)) // ByteRate
        dataOut.writeShort(java.lang.Short.reverseBytes(blockAlign.toShort()).toInt()) // BlockAlign
        dataOut.writeShort(java.lang.Short.reverseBytes(bitsPerSample.toShort()).toInt()) // BitsPerSample

        // "data" sub-chunk
        dataOut.writeBytes("data")
        dataOut.writeInt(Integer.reverseBytes(dataSize)) // Subchunk2Size
        
        // Write the actual PCM data
        dataOut.write(pcmData)

        dataOut.close()
        return out.toByteArray()
    }
}
