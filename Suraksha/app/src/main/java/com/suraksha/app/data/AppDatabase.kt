package com.suraksha.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrustedContact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // This tells the database which DAOs (query lists) it will use.
    abstract fun contactDao(): ContactDao

    companion object {
        // This @Volatile variable is the single instance of the database.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // This function gets or creates the database.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "suraksha_database" // This will be the name of your database file
                ).allowMainThreadQueries() // <-- HERE IS THE FIX
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}