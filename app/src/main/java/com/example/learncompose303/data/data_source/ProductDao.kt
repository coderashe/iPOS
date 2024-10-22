package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Inventory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE productId = :id")
    suspend fun getProductById(id: Long): Inventory?

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Inventory>>

    // Fetch all Products once (without Flow)
    @Query("SELECT * FROM products")
    suspend fun getAllProductsOnce(): List<Inventory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(inventory: Inventory): Long


    // Insert multiple Products
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(inventories: List<Inventory>): List<Long> // Return list of inserted product IDs
//    suspend fun insertProducts(product: List<Product>)

    @Update
    suspend fun updateProduct(inventory: Inventory)

    @Delete
    suspend fun deleteProduct(inventory: Inventory)
}
