package com.example.learncompose303.data.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyBlueish
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.ui.theme.MyPurple
import com.example.learncompose303.ui.theme.Pink80

@Entity
data class Note(
    @PrimaryKey
    val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
) {
    companion object {
        val noteColors = listOf(Pink80, MyBlueish, MyGray, GreenColor, MyPurple)
    }
}

class InvalidNoteException(message: String) : Exception(message)