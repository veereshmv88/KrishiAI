package com.krishiai.app.core.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.krishiai.app.data.local.prefs.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    init {
        scope.launch {
            dataStoreManager.languageFlow.collect { langCode ->
                _currentLanguage.value = langCode
                setAppLocale(langCode)
            }
        }
    }

    fun setLanguage(langCode: String) {
        scope.launch {
            dataStoreManager.setLanguage(langCode)
            // Note: The flow collection in init will automatically update _currentLanguage and setAppLocale
        }
    }

    private fun setAppLocale(langCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
