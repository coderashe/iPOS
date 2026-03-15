package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.PriceList
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceListDao {
    @Query("SELECT * FROM price_list WHERE priceListId = :id")
    suspend fun getPriceListById(id: Long): PriceList?

    @Query("SELECT * FROM price_list WHERE productId = :productId")
    suspend fun getPriceListByProductId(productId: Long): PriceList?

    @Query("SELECT * FROM price_list")
    fun getAllPriceLists(): Flow<List<PriceList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceList(priceList: PriceList): Long

    @Query("UPDATE price_list SET sellingPrice = :price, productDescription = :productDescription WHERE productId = :productId")
    suspend fun insertCurrentPriceList(
        price: Double,
        productDescription: String,
        productId: Long
    ): Int

    // Insert multiple PriceList items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceLists(priceLists: List<PriceList>)

    @Update
    suspend fun updatePriceList(priceList: PriceList)

    @Update
    suspend fun updatePriceLists(priceLists: List<PriceList>)

    @Delete
    suspend fun deletePriceList(priceList: PriceList)

    @Query("SELECT * FROM price_list WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<PriceList>

    @Query("UPDATE price_list SET isSynced = 1 WHERE priceListId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM price_list WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
