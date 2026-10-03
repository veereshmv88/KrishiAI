package com.krishiai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.data.local.prefs.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val languageManager: com.krishiai.app.core.language.LanguageManager
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = dataStoreManager.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentLanguage: StateFlow<String> = languageManager.currentLanguage

    val notificationsEnabled: StateFlow<Boolean> = dataStoreManager.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val offlineSyncEnabled: StateFlow<Boolean> = dataStoreManager.offlineSyncFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDarkMode(enabled)
        }
    }

    fun setLanguage(langCode: String) {
        languageManager.setLanguage(langCode)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setNotificationsEnabled(enabled)
        }
    }
    
    fun setOfflineSync(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setOfflineSync(enabled)
        }
    }
}
