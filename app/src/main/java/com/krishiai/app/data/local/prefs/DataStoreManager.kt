package com.krishiai.app.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "krishi_settings")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val OFFLINE_SYNC = booleanPreferencesKey("offline_sync")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    val isDarkModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    val languageFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_CODE] ?: "en"
    }
    
    val notificationsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }
    
    val offlineSyncFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[OFFLINE_SYNC] ?: true
    }

    val userRoleFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ROLE]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = languageCode
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setOfflineSync(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OFFLINE_SYNC] = enabled
        }
    }

    suspend fun setUserRole(role: String) {
        dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }
}
