package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getNotes()
    }

    override suspend fun getNoteByID(id: Int): Note? {
        return noteDao.getNoteByID(id)
    }

    override suspend fun insertNote(note: Note) {
        return noteDao.insertNote(note)
    }

    override suspend fun updateNote(note: Note) {
        return noteDao.updateNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        return noteDao.deleteNote(note)
    }

}
