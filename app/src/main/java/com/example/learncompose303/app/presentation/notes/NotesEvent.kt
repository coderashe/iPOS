package com.example.learncompose303.app.presentation.notes

import com.example.learncompose303.data.domain.model.Note

sealed class NotesEvent {

    data class Order(val noteOrder: Order) : NotesEvent()
    data class DeleteOrder(val note: Note) : NotesEvent()
    object RestoreNote : NotesEvent()
    object ToggleOrderSection : NotesEvent()
}