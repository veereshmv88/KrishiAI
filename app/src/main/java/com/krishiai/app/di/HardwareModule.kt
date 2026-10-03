package com.krishiai.app.di

import android.content.Context
import com.krishiai.app.data.hardware.LocationTrackerImpl
import com.krishiai.app.data.hardware.OcrScannerImpl
import com.krishiai.app.data.hardware.VoiceRecognizerImpl
import com.krishiai.app.domain.hardware.LocationTracker
import com.krishiai.app.domain.hardware.OcrScanner
import com.krishiai.app.domain.hardware.VoiceRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HardwareModule {

    @Provides
    @Singleton
    fun provideLocationTracker(@ApplicationContext context: Context): LocationTracker {
        return LocationTrackerImpl(context)
    }

    @Provides
    @Singleton
    fun provideVoiceRecognizer(@ApplicationContext context: Context): VoiceRecognizer {
        return VoiceRecognizerImpl(context)
    }

    @Provides
    @Singleton
    fun provideOcrScanner(): OcrScanner {
        return OcrScannerImpl()
    }
}
