package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM Payment WHERE invoiceId = :invoiceId")
    suspend fun getPaymentsForInvoice(invoiceId: Long): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("SELECT * FROM payment WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<Payment>

    @Query("UPDATE payment SET isSynced = 1 WHERE paymentId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM payment WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
