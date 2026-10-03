package com.krishiai.app.di

import com.krishiai.app.data.ai.AIDiseaseDetectorImpl
import com.krishiai.app.data.ai.AIPricePredictorImpl
import com.krishiai.app.data.ai.AIProfitEstimatorImpl
import com.krishiai.app.data.ai.AINegotiationAssistantImpl
import com.krishiai.app.domain.ai.IAIDiseaseDetector
import com.krishiai.app.domain.ai.IAINegotiationAssistant
import com.krishiai.app.domain.ai.IAIPricePredictor
import com.krishiai.app.domain.ai.IAIProfitEstimator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideAIPricePredictor(): IAIPricePredictor {
        return AIPricePredictorImpl()
    }

    @Provides
    @Singleton
    fun provideAIDiseaseDetector(@ApplicationContext context: android.content.Context): IAIDiseaseDetector {
        return AIDiseaseDetectorImpl(context)
    }

    @Provides
    @Singleton
    fun provideAIProfitEstimator(): IAIProfitEstimator {
        return AIProfitEstimatorImpl()
    }

    @Provides
    @Singleton
    fun provideAINegotiationAssistant(): IAINegotiationAssistant {
        return AINegotiationAssistantImpl()
    }

    @Provides
    @Singleton
    fun provideAIQualityGrader(@ApplicationContext context: android.content.Context): com.krishiai.app.domain.ai.IAIQualityGrader {
        return com.krishiai.app.data.ai.AIQualityGraderImpl(context)
    }

    @Provides
    @Singleton
    fun provideAIVoiceAssistant(impl: com.krishiai.app.data.ai.AIVoiceAssistantImpl): com.krishiai.app.domain.ai.IAIVoiceAssistant {
        return impl
    }

    @Provides
    @Singleton
    fun provideSpeechToTextEngine(impl: com.krishiai.app.data.ai.WhisperSpeechToTextEngine): com.krishiai.app.domain.ai.SpeechToTextEngine {
        return impl
    }

    @Provides
    @Singleton
    fun provideDhVaaniTtsRepository(
        @ApplicationContext context: android.content.Context,
        api: com.krishiai.app.data.remote.dhvaani.DhVaaniApiService
    ): com.krishiai.app.data.remote.dhvaani.DhVaaniTtsRepository {
        return com.krishiai.app.data.remote.dhvaani.DhVaaniTtsRepositoryImpl(context, api)
    }

    @Provides
    @Singleton
    fun provideTextToSpeechEngine(impl: com.krishiai.app.data.ai.DhVaaniTextToSpeechEngine): com.krishiai.app.domain.ai.TextToSpeechEngine {
        return impl
    }

    @Provides
    @Singleton
    fun provideAIReasoningEngine(impl: com.krishiai.app.data.ai.GeminiAIReasoningEngine): com.krishiai.app.domain.ai.AIReasoningEngine {
        return impl
    }

    @Provides
    @Singleton
    fun provideAIContextEngine(impl: com.krishiai.app.data.ai.AIContextEngineImpl): com.krishiai.app.domain.ai.AIContextEngine {
        return impl
    }

    @Provides
    @Singleton
    fun provideAIIntentRouter(impl: com.krishiai.app.data.ai.AIIntentRouterImpl): com.krishiai.app.domain.ai.AIIntentRouter {
        return impl
    }
}
