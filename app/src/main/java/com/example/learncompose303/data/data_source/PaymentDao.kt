package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Payment

@Dao
interface PaymentDao {
    @Query("SELECT * FROM Payment WHERE invoiceId = :invoiceId")
    suspend fun getPaymentsForInvoice(invoiceId: Long): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long
}
