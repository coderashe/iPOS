package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ShoeSize
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoeSizeDao {
    @Query("SELECT * FROM shoe_sizes")
    fun getAllShoeSizes(): Flow<List<ShoeSize>>


    @Query("SELECT * FROM shoe_sizes")
    suspend fun getAllShoeSizesOnce(): List<ShoeSize>

    @Query("SELECT * FROM shoe_sizes WHERE shoeSizeId = :id")
    suspend fun getShoeSizeById(id: Long): ShoeSize?

    @Query("SELECT SUM(quantity) FROM shoe_sizes WHERE brandID = :brandId")
    suspend fun getTotalQuantityByBrand(brandId: Long): Int

    @Query("SELECT * FROM shoe_sizes WHERE brandID = :labelId")
    fun getShoeSizesByLabel(labelId: Long): Flow<List<ShoeSize>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoeSize(shoeSize: ShoeSize): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoeSizes(shoeSizes: List<ShoeSize>): List<Long>

    @Update
    suspend fun updateShoeSize(shoeSize: ShoeSize)

    @Delete
    suspend fun deleteShoeSize(shoeSize: ShoeSize)
}
