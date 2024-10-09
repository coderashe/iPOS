package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.MonthlySales

import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoice WHERE invoiceId = :id")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoice")
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


    @Query("SELECT strftime('%m', invoiceDate / 1000, 'unixepoch') AS month, SUM(totalAmount) AS totalSales FROM invoice GROUP BY month")
    fun getSalesPerMonth(): Flow<List<MonthlySales>>

}
