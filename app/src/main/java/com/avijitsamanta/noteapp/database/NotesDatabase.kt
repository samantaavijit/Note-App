package com.avijitsamanta.noteapp.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.avijitsamanta.noteapp.dao.NoteDao
import com.avijitsamanta.noteapp.entities.Note
import com.avijitsamanta.noteapp.helper.Converters

@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao

    companion object {

        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getDatabase(context: Context): NotesDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}