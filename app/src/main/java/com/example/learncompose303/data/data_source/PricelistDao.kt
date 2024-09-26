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

    // Insert multiple PriceList items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceLists(priceLists: List<PriceList>)

    @Update
    suspend fun updatePriceList(priceList: PriceList)

    @Delete
    suspend fun deletePriceList(priceList: PriceList)
}
