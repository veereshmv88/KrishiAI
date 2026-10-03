package com.krishiai.app.disease.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DiseaseScanEntity::class], version = 1, exportSchema = false)
abstract class DiseaseDatabase : RoomDatabase() {
    abstract fun diseaseDao(): DiseaseDao
}
