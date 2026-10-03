package com.krishiai.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "krishi_settings")

class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_ROLE = stringPreferencesKey("user_role")
        val APP_LANGUAGE = stringPreferencesKey("app_language") // "en" or "kn"
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val CACHED_STATE = stringPreferencesKey("cached_state")
        val CACHED_DISTRICT = stringPreferencesKey("cached_district")
        val CACHED_TALUK = stringPreferencesKey("cached_taluk")
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE] ?: "en"
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language
        }
    }

    // Other settings omitted for brevity, but they follow the same pattern
}
