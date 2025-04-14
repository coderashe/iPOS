package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.learncompose303.data.domain.ProductWithBrand
import com.example.learncompose303.data.entities.Products
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products")
    fun getProductsWithBrand(): Flow<List<ProductWithBrand>>

    //    handles case-sensitive
    @Query("SELECT * FROM products WHERE LOWER(productDescription) = LOWER(:productName) AND shoeSizeId = :shoeSizeId LIMIT 1")
    suspend fun getProductByNameAndSize(productName: String, shoeSizeId: Long): Products?


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
    suspend fun insertProducts(products: List<Products>): List<Long> // Return list of inserted product IDs

    @Update
    suspend fun updateProduct(products: Products)

    @Query("UPDATE products SET productQty = :newQty WHERE productId = :productId")
    suspend fun updateProductQuantity(productId: Long, newQty: Long)

    @Delete
    suspend fun deleteProduct(products: Products)

    @Query("SELECT * FROM products WHERE productQty = :storeID")
    suspend fun getProductsByStoreID(storeID: String): List<Products>

    @Query("SELECT * FROM products WHERE brandID = :brandID")
    suspend fun getProductsByBrandID(brandID: Long): List<Products>

    @Query("SELECT * FROM products WHERE shoeSizeId = :shoeSizeId")
    suspend fun getProductsByShoeSizeId(shoeSizeId: Long): List<Products>

    @Query("SELECT * FROM products WHERE productQty = :storeID AND brandID = :brandID AND shoeSizeId = :shoeSizeId")
    suspend fun getProductsByStoreAndBrandAndShoeSize(
        storeID: String,
        brandID: Long,
        shoeSizeId: Long
    ): List<Products>
}
