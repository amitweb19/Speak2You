package com.amitweb19.speak2you.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.amitweb19.speak2you.data.*

@Database(entities = [Questionaire::class, Question::class, Mistake::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao() : QuestionDao
    abstract fun questionaireDao() : QuestionaireDao
    abstract fun mistakeDao() : MistakeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speak2you_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}