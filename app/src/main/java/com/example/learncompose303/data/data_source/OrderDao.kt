package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.learncompose303.data.entities.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    // Insert a new order
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    // Get all orders
    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<Order>>

    // Get a specific order by ID
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): Order?

    // Delete an order
    @Delete
    suspend fun deleteOrder(order: Order)

    // Clear all orders
    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}
