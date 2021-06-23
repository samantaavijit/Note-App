package com.avijitsamanta.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.avijitsamanta.noteapp.database.NotesDatabase
import com.avijitsamanta.noteapp.entities.Note
import com.avijitsamanta.noteapp.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    val allNodes: LiveData<List<Note>>
    private val repository: NoteRepository

    init {
        val dao = NotesDatabase.getDatabase(application).getNoteDao()
        repository = NoteRepository(dao)
        allNodes = repository.allNotes
    }

    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(note)
    }

    fun searchNote(key: String?): LiveData<List<Note>> {
        return repository.search(key)
    }

}