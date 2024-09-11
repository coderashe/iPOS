package com.example.learncompose303.app.presentation.notes

import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.domain.util.NoteOrder
import com.example.learncompose303.data.domain.util.OrderType

data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false
)
