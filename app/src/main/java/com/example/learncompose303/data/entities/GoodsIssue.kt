package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goods_issue")
data class GoodsIssue(
    @PrimaryKey(autoGenerate = true)
    val issueId: Long = 0,
    val productId: Long,
    val qty: Int,
    val issueReason: String, // E.g., Expired, Waste, etc.
    val issueDate: Long
)
