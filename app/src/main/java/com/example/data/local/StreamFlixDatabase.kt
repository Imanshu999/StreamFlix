package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WatchHistoryEntity::class, DownloadEntity::class, ReviewEntity::class],
    version = 2,
    exportSchema = false
)
abstract class StreamFlixDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: StreamFlixDatabase? = null

        fun getDatabase(context: Context): StreamFlixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreamFlixDatabase::class.java,
                    "streamflix_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
