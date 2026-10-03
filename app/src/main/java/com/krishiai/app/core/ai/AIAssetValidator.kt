package com.krishiai.app.core.ai

import android.content.Context
import java.io.File

object AIAssetValidator {
    
    fun validateAssets(context: Context, assets: List<String>): Boolean {
        return assets.all { assetPath ->
            try {
                context.assets.open(assetPath).use { true }
            } catch (e: Exception) {
                false
            }
        }
    }
}
