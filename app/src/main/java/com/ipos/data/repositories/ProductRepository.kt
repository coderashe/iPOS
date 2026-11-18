package com.ipos.data.repositories

import com.ipos.data.data_source.ProductDao
import com.ipos.data.domain.ProductWithBrand
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ProductRepository {
    suspend fun findProductByAttributes(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products?

    fun getProductsWithBrand(): Flow<List<ProductWithBrand>>

    suspend fun getProductByNameAndSize(productName: String, shoeSizeId: Long): Products?
    suspend fun updateProductQuantity(productId: Long, newQty: Long)
    fun getAllProducts(): Flow<List<Products>>
    suspend fun getProductById(id: Long): Products?
    suspend fun insertProduct(products: Products): Long
    suspend fun updateProduct(products: Products)
    suspend fun deleteProduct(products: Products)
    suspend fun insertProducts(products: List<Products>): List<Long> // Return list of inserted product IDs
    suspend fun updateProducts(products: List<Products>)
    suspend fun getAllProductsOnce(): List<Products>
    suspend fun getProductsByStoreID(storeID: String): List<Products>
    suspend fun getProductsByBrandID(brandID: Long): List<Products>
    suspend fun getProductsByShoeSizeId(shoeSizeId: Long): List<Products>
    suspend fun getProductsByStoreAndBrandAndShoeSize(
        storeID: String,
        brandID: Long,
        shoeSizeId: Long
    ): List<Products>

    fun getSingleProductById(productID: Long): Flow<Products?>

    suspend fun getProductByExactMatch(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products?

    suspend fun getProductByCaseInsensitiveMatch(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products?

    suspend fun getProductByContainsMatch(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products?

}

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val syncManager: com.ipos.network.SyncManager,
    private val deviceSession: com.ipos.app.core.DeviceSession
) : ProductRepository {

    companion object {
        private const val TAG = "ProductRepositoryImpl"
    }


    override suspend fun getProductByContainsMatch(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByContainsMatch(brandId, shoeSizeId, description)
                ?.takeIf { it.brandID == brandId }
        }
    }

    override suspend fun getProductByCaseInsensitiveMatch(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByCaseInsensitiveMatch(brandId, shoeSizeId, description)
                ?.takeIf { it.brandID == brandId }
        }
    }

    override suspend fun getProductByExactMatch(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByExactMatch(brandId, shoeSizeId, description)
                ?.takeIf { it.brandID == brandId }
        }
    }

    override suspend fun findProductByAttributes(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByNameAndSize(description, shoeSizeId)
                ?.takeIf { it.brandID == brandId }
        }
    }

    override fun getProductsWithBrand(): Flow<List<ProductWithBrand>> {
        return productDao.getProductsWithBrand()

    }

    override fun getAllProducts(): Flow<List<Products>> {
        return productDao.getAllProducts()
    }

    override suspend fun updateProductQuantity(productId: Long, newQty: Long) {
        return withContext(Dispatchers.IO) {
            productDao.updateProductQuantity(productId, newQty)
        }
    }

    override suspend fun insertProduct(products: Products): Long {
        return withContext(Dispatchers.IO) {
            // Mark for sync by setting lastUpdated to null
            val productToInsert = products.copy(lastUpdated = null)
            val productId = productDao.insertProduct(productToInsert)
            val prefix = if (deviceSession.storeId.isNotBlank()) deviceSession.storeId else "PROD"
            val productCode = "${prefix}_$productId"
            productDao.updateProduct(
                productToInsert.copy(
                    productId = productId,
                    productCode = productCode
                )
            )
            
            // Automatically trigger sync for the new product
            syncManager.scheduleSingleProductSync(productId)
            android.util.Log.d(TAG, "Auto-sync scheduled for new product: $productId")
            
            productId
        }
    }

    override suspend fun getProductById(id: Long): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductById(id)
        }
    }

    override fun getSingleProductById(productID: Long): Flow<Products?> {
        return productDao.getSingleProductById(productID)
    }

    override suspend fun getProductByNameAndSize(productName: String, shoeSizeId: Long): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByNameAndSize(productName, shoeSizeId)
        }
    }

    override suspend fun updateProduct(products: Products) {
        return withContext(Dispatchers.IO) {
            // Mark for sync by setting lastUpdated to null
            val productToUpdate = products.copy(lastUpdated = null)
            productDao.updateProduct(productToUpdate)
            
            // Automatically trigger sync for the updated product
            syncManager.scheduleSingleProductSync(products.productId)
            android.util.Log.d(TAG, "Auto-sync scheduled for updated product: ${products.productId}")
        }
    }

    override suspend fun deleteProduct(products: Products) {
        return withContext(Dispatchers.IO) {
            productDao.deleteProduct(products)
        }
    }

    override suspend fun insertProducts(products: List<Products>): List<Long> {
        return withContext(Dispatchers.IO) {
            // Mark all products for sync
            val productsToInsert = products.map { it.copy(lastUpdated = null) }
            val insertedIds = productDao.insertProducts(productsToInsert)
            
            // Trigger batch sync if multiple products inserted
            if (insertedIds.isNotEmpty()) {
                syncManager.scheduleProductBatchSync()
                android.util.Log.d(TAG, "Auto-sync scheduled for ${insertedIds.size} new products")
            }
            
            insertedIds
        }
    }

    override suspend fun updateProducts(products: List<Products>) {
        return withContext(Dispatchers.IO) {
            // Mark all products for sync
            val productsToUpdate = products.map { it.copy(lastUpdated = null) }
            productDao.updateProducts(productsToUpdate)
            
            // Trigger batch sync if multiple products updated
            if (products.isNotEmpty()) {
                syncManager.scheduleProductBatchSync()
                android.util.Log.d(TAG, "Auto-sync scheduled for ${products.size} updated products")
            }
        }
    }

    override suspend fun getAllProductsOnce(): List<Products> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProductsOnce()
        }
    }


    override suspend fun getProductsByStoreID(storeID: String): List<Products> {
        return withContext(Dispatchers.IO) {
            productDao.getProductsByStoreID(storeID)
        }
    }

    override suspend fun getProductsByBrandID(brandID: Long): List<Products> {
        return withContext(Dispatchers.IO) {
            productDao.getProductsByBrandID(brandID)
        }
    }

    override suspend fun getProductsByShoeSizeId(shoeSizeId: Long): List<Products> {
        return withContext(Dispatchers.IO) {
            productDao.getProductsByShoeSizeId(shoeSizeId)
        }
    }

    override suspend fun getProductsByStoreAndBrandAndShoeSize(
        storeID: String,
        brandID: Long,
        shoeSizeId: Long
    ): List<Products> {
        return withContext(Dispatchers.IO) {
            productDao.getProductsByStoreAndBrandAndShoeSize(storeID, brandID, shoeSizeId)
        }
    }

}
