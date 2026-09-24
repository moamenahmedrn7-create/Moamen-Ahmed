package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FollowerEntity
import com.example.data.model.NotificationAlertEntity
import com.example.data.model.TaskEntity

@Database(
    entities = [
        FollowerEntity::class,
        TaskEntity::class,
        NotificationAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MonjezDatabase : RoomDatabase() {
    abstract fun followerDao(): FollowerDao
    abstract fun taskDao(): TaskDao
    abstract fun notificationAlertDao(): NotificationAlertDao

    companion object {
        @Volatile
        private var INSTANCE: MonjezDatabase? = null

        fun getDatabase(context: Context): MonjezDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MonjezDatabase::class.java,
                    "monjez_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
