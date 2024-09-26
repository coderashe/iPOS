package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE productId = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    // Fetch all Products once (without Flow)
    @Query("SELECT * FROM products")
    suspend fun getAllProductsOnce(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long


    // Insert multiple Products
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long> // Return list of inserted product IDs
//    suspend fun insertProducts(product: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}
