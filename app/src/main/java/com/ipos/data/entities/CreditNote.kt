package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_notes")
data class CreditNote(
    @PrimaryKey(autoGenerate = true)
    val creditNoteId: Long = 0,
    val productId: Long,
    val customerId: Long,
    val qtyReturned: Double,
    val reason: String,
    val returnDate: Long
)
