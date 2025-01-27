package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.Products
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE productId = :id")
    suspend fun getProductById(id: Long): Products?

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Products>>

    // Fetch all Products once (without Flow)
    @Query("SELECT * FROM products")
    suspend fun getAllProductsOnce(): List<Products>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(products: Products): Long


    // Insert multiple Products
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(inventories: List<Products>): List<Long> // Return list of inserted product IDs
//    suspend fun insertProducts(product: List<Product>)

    @Update
    suspend fun updateProduct(products: Products)

    @Delete
    suspend fun deleteProduct(products: Products)

    @Query("SELECT * FROM products WHERE storeID = :storeID")
    suspend fun getProductsByStoreID(storeID: String): List<Products>

    @Query("SELECT * FROM products WHERE brandID = :brandID")
    suspend fun getProductsByBrandID(brandID: Long): List<Products>

    @Query("SELECT * FROM products WHERE shoeSizeId = :shoeSizeId")
    suspend fun getProductsByShoeSizeId(shoeSizeId: Long): List<Products>

    @Query("SELECT * FROM products WHERE storeID = :storeID AND brandID = :brandID AND shoeSizeId = :shoeSizeId")
    suspend fun getProductsByStoreAndBrandAndShoeSize(
        storeID: String,
        brandID: Long,
        shoeSizeId: Long
    ): List<Products>
}
