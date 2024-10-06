package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceLineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceLine(invoiceLine: InvoiceLine)

    @Query("SELECT * FROM invoice_lines")
    fun getAllInvoiceLines(): Flow<List<InvoiceLine>>

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :id")
    suspend fun getInvoiceLineById(id: Long): InvoiceLine?

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceLinesByInvoiceId(invoiceId: Long): List<InvoiceLine>

}

