package com.krishiai.app.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.krishiai.app.data.model.WeatherData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_cache")

@Singleton
class WeatherLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.weatherDataStore

    companion object {
        val WEATHER_DATA_KEY = stringPreferencesKey("cached_weather_data")
    }

    suspend fun saveWeather(weatherData: WeatherData) {
        val json = gson.toJson(weatherData)
        dataStore.edit { prefs ->
            prefs[WEATHER_DATA_KEY] = json
        }
    }

    suspend fun getCachedWeather(): WeatherData? {
        val json = dataStore.data.map { prefs -> prefs[WEATHER_DATA_KEY] }.firstOrNull()
        return if (json != null) {
            try {
                gson.fromJson(json, WeatherData::class.java).copy(isCached = true)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    suspend fun clearCache() {
        dataStore.edit { prefs ->
            prefs.remove(WEATHER_DATA_KEY)
        }
    }
}
