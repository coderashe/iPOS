package com.example.learncompose303.data.data_source

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.learncompose303.data.entities.CartItem

@Dao
interface CartItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem): Long

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId")
    fun getCartItemsForCustomer(customerId: Long): Flow<List<CartItem>>

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCartForCustomer(customerId: Long)

    @Query("SELECT * FROM cart_items WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<CartItem>

    @Query("UPDATE cart_items SET isSynced = 1 WHERE cartItemId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM cart_items WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
