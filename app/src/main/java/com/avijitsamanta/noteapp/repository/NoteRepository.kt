package com.avijitsamanta.noteapp.repository

import androidx.lifecycle.LiveData
import com.avijitsamanta.noteapp.dao.NoteDao
import com.avijitsamanta.noteapp.entities.Note


class NoteRepository(private val noteDao: NoteDao) {
    val allNotes = noteDao.getAllNotes()

    suspend fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }

    fun search(key: String?): LiveData<List<Note>> {
        return noteDao.search("%$key%")
    }
}