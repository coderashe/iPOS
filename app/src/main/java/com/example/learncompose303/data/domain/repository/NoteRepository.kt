package com.example.learncompose303.data.domain.repository

import com.example.learncompose303.data.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getNotes(): Flow<List<Note>>

    suspend fun getNoteByID(id: Int): Note?

    suspend fun insertNote(Note: Note)

    suspend fun updateNote(Note: Note)

    suspend fun deleteNote(Note: Note)
}
