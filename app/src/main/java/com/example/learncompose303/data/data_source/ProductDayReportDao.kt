package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.ProductDayReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDayReportDao {

    @Query("SELECT * FROM product_day_reports WHERE reportId = :id")
    suspend fun getProductDayReportById(id: Long): ProductDayReport?

    @Query("SELECT * FROM product_day_reports")
    fun getAllProductDayReports(): Flow<List<ProductDayReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductDayReport(productDayReport: ProductDayReport): Long

    @Update
    suspend fun updateProductDayReport(productDayReport: ProductDayReport)

    @Delete
    suspend fun deleteProductDayReport(productDayReport: ProductDayReport)
}
