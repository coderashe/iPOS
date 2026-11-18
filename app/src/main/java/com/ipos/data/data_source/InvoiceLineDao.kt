package com.ipos.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ipos.data.entities.HourPerformance
import com.ipos.data.entities.InvoiceLine
import com.ipos.data.entities.ProductPerformance
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceLineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceLine(invoiceLine: InvoiceLine): Long

    @Query("SELECT * FROM invoice_lines")
    fun getAllInvoiceLines(): Flow<List<InvoiceLine>>

    @Query("SELECT * FROM invoice_lines WHERE synced = 0")
    suspend fun getUnsyncedInvoiceLines(): List<InvoiceLine>

    @Query("UPDATE invoice_lines SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :id")
    suspend fun getInvoiceLineById(id: Long): InvoiceLine?

    @Query("SELECT * FROM invoice_lines WHERE id = :id")
    suspend fun getInvoiceLineByIdOnce(id: Int): InvoiceLine?

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceLinesByInvoiceId(invoiceId: Long): List<InvoiceLine>

    @Update
    suspend fun updateInvoiceLine(invoiceLine: InvoiceLine)

    @Query("UPDATE invoice_lines SET deviceId = :deviceId, storeId = :storeId WHERE deviceId = '' OR deviceId IS NULL OR storeId != :storeId")
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String)

    @Query(
        """
    SELECT productId AS productID, 
           productName AS productName, 
           SUM(productQtySold) AS totalSold, 
           currentStock AS currentStock, 
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
    LIMIT 5
    """
    )
    fun getPeakSalesHours(): Flow<List<HourPerformance>>

}
