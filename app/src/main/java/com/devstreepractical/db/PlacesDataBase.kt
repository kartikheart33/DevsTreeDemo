package com.devstreepractical.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devstreepractical.ui.PlacesDao
import com.devstreepractical.ui.adapter.PlacesDataModel

@Database(
    entities = [PlacesDataModel::class],
    version = 1,
    exportSchema = true
)
abstract class PlacesDataBase : RoomDatabase() {

    abstract fun placesDao(): PlacesDao

    companion object {

        @Volatile
        private var INSTANCE: PlacesDataBase? = null

        fun getDatabase(context: Context): PlacesDataBase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = buildDatabase(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): PlacesDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                PlacesDataBase::class.java,
                "places_database"
            ).build()
        }
    }
}