package com.app.citytailor.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.citytailor.data.room.converter.DateConverter
import com.app.citytailor.data.room.dao.SavedTravelPlanDao
import com.app.citytailor.data.room.entity.SavedTravelPlan

@Database(
    entities = [SavedTravelPlan::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedTravelPlanDao(): SavedTravelPlanDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "citytailor_database"
                ).build()
                instance = newInstance
                newInstance
            }
        }
    }
}
