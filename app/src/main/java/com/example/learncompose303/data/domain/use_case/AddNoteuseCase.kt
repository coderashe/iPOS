package com.example.learncompose303.data.domain.use_case

import com.example.learncompose303.data.domain.model.InvalidNoteException
import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.domain.repository.NoteRepository
import kotlin.jvm.Throws

class AddNoteuseCase(
    private val noteRepository: NoteRepository
) {
    @Throws(InvalidNoteException::class)
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank()) {
            throw InvalidNoteException("Note title cannot be empty!")
        }
        if (note.content.isBlank()) {
            throw InvalidNoteException("Note content cannot be empty!")
        }
        noteRepository.insertNote(note)
    }
}