package com.tcm.traditionalchinesemedician.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for the application
 */
@Database(
    entities = [HerbEntity::class, VersionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(HerbConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun herbDao(): HerbDao
    abstract fun versionDao(): VersionDao
    
    companion object {
        private const val DATABASE_NAME = "tcm_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For simplicity, destroy and rebuild DB on schema change
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 