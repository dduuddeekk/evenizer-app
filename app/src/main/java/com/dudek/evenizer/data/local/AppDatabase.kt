package com.dudek.evenizer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dudek.evenizer.data.local.dao.EventDao
import com.dudek.evenizer.data.local.dao.RemoteKeysDao
import com.dudek.evenizer.data.local.entity.EventEntity
import com.dudek.evenizer.data.local.entity.RemoteKeys

@Database(
    entities = [EventEntity::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "evenizer_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
