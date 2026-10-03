package com.krishiai.app.di

import com.krishiai.app.data.remote.weather.WeatherApiService
import com.krishiai.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(okHttpClient: OkHttpClient): WeatherApiService {
        val retrofit = Retrofit.Builder()
            // We use absolute URLs in WeatherApiService, but Retrofit needs a dummy base URL if not all endpoints use absolute URLs.
            // Using a generic dummy base url. The @GET inside WeatherApiService has the full URL anyway.
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApmcApi(): com.krishiai.app.data.remote.ApmcApi {
        return com.krishiai.app.data.remote.MockApmcApiImpl()
    }

    @Provides
    @Singleton
    fun provideAgmarknetApiService(okHttpClient: OkHttpClient): com.krishiai.app.data.network.AgmarknetApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.data.gov.in/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(com.krishiai.app.data.network.AgmarknetApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideYouTubeApiService(okHttpClient: OkHttpClient): com.krishiai.app.disease.data.remote.YouTubeApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(com.krishiai.app.disease.data.remote.YouTubeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWhisperApiService(loggingInterceptor: HttpLoggingInterceptor): com.krishiai.app.data.remote.whisper.WhisperApiService {
        val whisperClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(com.krishiai.app.utils.Constants.WHISPER_BASE_URL)
            .client(whisperClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        return retrofit.create(com.krishiai.app.data.remote.whisper.WhisperApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDhVaaniApiService(loggingInterceptor: HttpLoggingInterceptor): com.krishiai.app.data.remote.dhvaani.DhVaaniApiService {
        val ttsClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS) // Up to 3 mins for CPU inference
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(com.krishiai.app.utils.Constants.WHISPER_BASE_URL)
            .client(ttsClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        return retrofit.create(com.krishiai.app.data.remote.dhvaani.DhVaaniApiService::class.java)
    }
}
