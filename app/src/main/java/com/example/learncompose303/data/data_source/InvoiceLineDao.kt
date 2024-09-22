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

    @Query("SELECT * FROM invoiceline")
    fun getAllInvoiceLines(): Flow<List<InvoiceLine>>

    @Query("SELECT * FROM invoiceline WHERE invoiceID = :id")
    suspend fun getInvoiceLineById(id: Long): InvoiceLine?
}

