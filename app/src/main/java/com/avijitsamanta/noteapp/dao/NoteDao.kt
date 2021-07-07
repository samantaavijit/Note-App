package com.avijitsamanta.noteapp.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.avijitsamanta.noteapp.entities.Note

@Dao
interface NoteDao {
    @Query("select * from notes order by time_stamp desc")
    fun getAllNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("select * from notes where lower(title) like :key or lower(note_text) like :key order by time_stamp desc")
    fun search(key: String?): LiveData<List<Note>>
}