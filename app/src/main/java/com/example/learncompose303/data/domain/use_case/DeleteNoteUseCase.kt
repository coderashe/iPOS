package com.example.learncompose303.data.domain.use_case

import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        return noteRepository.deleteNote(note)
    }
}