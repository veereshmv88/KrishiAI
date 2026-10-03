package com.krishiai.app.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object LocalStorageHelper {
    fun saveImageToCache(context: Context, bytes: ByteArray): String {
        val cacheDir = File(context.cacheDir, "crop_images")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use {
            it.write(bytes)
        }
        return file.absolutePath
    }
}
