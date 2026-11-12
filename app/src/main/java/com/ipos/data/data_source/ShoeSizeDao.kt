package com.ipos.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeSize
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoeSizeDao {
    @Query("SELECT * FROM shoe_sizes")
    fun getAllShoeSizes(): Flow<List<ShoeSize>>


    @Query("SELECT * FROM shoe_sizes")
    suspend fun getAllShoeSizesOnce(): List<ShoeSize>

    @Query("SELECT * FROM shoe_sizes WHERE shoeSizeId = :id")
    suspend fun getShoeSizeById(id: Long): ShoeSize?

    // Add to ShoeSizeDao
    @Query("SELECT * FROM shoe_sizes WHERE size = :size LIMIT 1")
    suspend fun getSizeByValue(size: Double): ShoeSize?

    @Query("SELECT * FROM shoe_sizes WHERE brandID = :brandId AND size = :size LIMIT 1")
    suspend fun getSizeByBrandAndValue(brandId: Long, size: Double): ShoeSize?

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

    @Query("UPDATE shoe_sizes SET quantity = :newQty WHERE shoeSizeId = :shoeSizeId")
    suspend fun updateShoeSizeQuantity(shoeSizeId: Long, newQty: Long)

    @Delete
    suspend fun deleteShoeSize(shoeSize: ShoeSize)

    @Query("SELECT * FROM shoe_sizes WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<ShoeSize>

    @Query("UPDATE shoe_sizes SET isSynced = 1 WHERE shoeSizeId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM shoe_sizes WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
