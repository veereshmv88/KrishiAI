package com.krishiai.app.disease.di

import android.content.Context
import androidx.room.Room
import com.krishiai.app.disease.data.DiseaseDao
import com.krishiai.app.disease.data.DiseaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiseaseDataModule {

    @Provides
    @Singleton
    fun provideDiseaseDatabase(@ApplicationContext context: Context): DiseaseDatabase {
        return Room.databaseBuilder(
            context,
            DiseaseDatabase::class.java,
            "disease_database"
        ).build()
    }

    @Provides
    fun provideDiseaseDao(database: DiseaseDatabase): DiseaseDao {
        return database.diseaseDao()
    }
}
