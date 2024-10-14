package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.HourPerformance
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.data.entities.ProductPerformance
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

    @Update
    suspend fun updateInvoiceLine(invoiceLine: InvoiceLine)

    @Query(
        """
    SELECT productId AS productID, 
           productName AS productName, 
           SUM(productQtySold) AS totalSold, 
           productPrice AS price 
    FROM invoice_lines 
    GROUP BY productId 
    ORDER BY totalSold DESC  -- Order by totalSold instead of totalRevenue
    LIMIT 5
    """
    )
    fun getTopPerformingProducts(): Flow<List<ProductPerformance>>


    @Query(
        """
    SELECT (invoiceDate / 1000) AS hour, -- Return the raw timestamp in seconds
           SUM(totalAmount) AS totalSales 
    FROM invoice_lines 
    GROUP BY hour 
    ORDER BY totalSales DESC
    """
    )
    fun getPeakSalesHours(): Flow<List<HourPerformance>>


}
