package com.krishiai.app.data.remote.dhvaani

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

interface DhVaaniTtsRepository {
    suspend fun synthesize(text: String, language: String): Result<Uri>
}

class DhVaaniTtsRepositoryImpl(
    private val context: Context,
    private val api: DhVaaniApiService
) : DhVaaniTtsRepository {

    override suspend fun synthesize(text: String, language: String): Result<Uri> {
        return try {
            val response = api.synthesize(TtsRequest(text, language))
            
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Empty response body"))
                
                // Create temporary file
                val tempFile = File(context.cacheDir, "dhvaani_tts_${UUID.randomUUID()}.wav")
                
                // Write stream to file using try/finally for cleanup
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(tempFile)
                    fos.write(body.bytes())
                    fos.flush()
                    
                    if (tempFile.exists() && tempFile.length() > 0) {
                        Result.success(Uri.fromFile(tempFile))
                    } else {
                        Result.failure(Exception("Failed to save valid WAV file"))
                    }
                } catch (e: Exception) {
                    if (tempFile.exists()) tempFile.delete()
                    Result.failure(e)
                } finally {
                    fos?.close()
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
