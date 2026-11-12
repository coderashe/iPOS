package com.ipos.data.data_source

import androidx.room.*
import com.ipos.data.entities.FailedInvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FailedInvoiceDao {
    
    @Query("SELECT * FROM failed_invoices ORDER BY timestamp DESC")
    fun getAllFailedInvoices(): Flow<List<FailedInvoiceItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFailedInvoices(items: List<FailedInvoiceItem>)
    
    @Delete
    suspend fun deleteFailedInvoice(item: FailedInvoiceItem)
    
    @Query("DELETE FROM failed_invoices")
    suspend fun clearAll()
    
    @Query("SELECT * FROM failed_invoices")
    suspend fun getAllFailedInvoicesOnce(): List<FailedInvoiceItem>
}
