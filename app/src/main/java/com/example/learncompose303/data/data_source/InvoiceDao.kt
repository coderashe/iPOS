package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceWithCurrency
import com.example.learncompose303.data.entities.MonthlySales
import com.example.learncompose303.data.entities.MonthlySalesWithCurrency

import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT i.totalAmount, i.currencyCode FROM invoice i")
    fun getInvoicesWithCurrency(): Flow<List<InvoiceWithCurrency>>

    @Query("SELECT * FROM invoice WHERE invoiceId = :id")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoice ORDER BY invoiceId DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoice WHERE invoiceDate >= :startDate")
    fun getInvoicesByDate(startDate: Long): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("SELECT SUM(totalAmount) FROM invoice")
    fun getTotalSales(): Flow<Double>

    @Query(
        """
    SELECT strftime('%m', invoiceDate / 1000, 'unixepoch') AS monthNumber, 
           SUM(totalAmount) AS totalSales 
    FROM invoice 
    GROUP BY monthNumber
"""
    )
    fun getSalesPerMonth(): Flow<List<MonthlySales>>

    @Query("""
    SELECT 
        strftime('%m', invoiceDate / 1000, 'unixepoch') AS monthNumber,
        totalAmount,
        currencyCode
    FROM invoice
""")
    fun getSalesPerMonthWithCurrency(): Flow<List<MonthlySalesWithCurrency>>
}
