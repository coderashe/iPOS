package com.example.learncompose303.data.data_source


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.InvoiceItem

@Dao
interface InvoiceItemDao {
    @Query("SELECT * FROM InvoiceItem WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItem): Long

    @Delete
    suspend fun deleteInvoiceItem(item: InvoiceItem)
}
