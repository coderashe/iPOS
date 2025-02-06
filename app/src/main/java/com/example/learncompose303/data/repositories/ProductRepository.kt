package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.entities.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ProductRepository {
    suspend fun getProductByNameAndSize(productName: String, shoeSizeId: Long): Products?
    suspend fun updateProductQuantity(productId: Long, newQty: Long)
    fun getAllProducts(): Flow<List<Products>>
    suspend fun getProductById(id: Long): Products?
    suspend fun insertProduct(products: Products): Long
    suspend fun updateProduct(products: Products)
    suspend fun deleteProduct(products: Products)
    suspend fun insertProducts(products: List<Products>): List<Long> // Return list of inserted product IDs
    suspend fun getAllProductsOnce(): List<Products>
    suspend fun getProductsByStoreID(storeID: String): List<Products>
    suspend fun getProductsByBrandID(brandID: Long): List<Products>
    suspend fun getProductsByShoeSizeId(shoeSizeId: Long): List<Products>
    suspend fun getProductsByStoreAndBrandAndShoeSize(
        storeID: String,
        brandID: Long,
        shoeSizeId: Long
    ): List<Products>

}

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun updateProductQuantity(productId: Long, newQty: Long) {
        return withContext(Dispatchers.IO) {
            productDao.updateProductQuantity(productId, newQty)
        }
    }

    override suspend fun insertProduct(products: Products): Long {
        return withContext(Dispatchers.IO) {
            val productId = productDao.insertProduct(products)
            val productCode = "ISTART_$productId"
            productDao.updateProduct(
                products.copy(
                    productId = productId,
                    productCode = productCode
                )
            )
            productId
        }
    }

    override fun getAllProducts(): Flow<List<Products>> {
        return productDao.getAllProducts()
    }

    override suspend fun getProductById(id: Long): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductById(id)
        }
    }

    override suspend fun getProductByNameAndSize(productName: String, shoeSizeId: Long): Products? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByNameAndSize(productName, shoeSizeId)
        }
    }

    override suspend fun updateProduct(products: Products) {
        return withContext(Dispatchers.IO) {
            productDao.updateProduct(products)
        }
    }

    override suspend fun deleteProduct(products: Products) {
        return withContext(Dispatchers.IO) {
            productDao.deleteProduct(products)
        }
    }

    override suspend fun insertProducts(products: List<Products>): List<Long> {
        return withContext(Dispatchers.IO) {
            productDao.insertProducts(products)
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


//    override suspend fun insertProduct(products: Products): Long {
//        return withContext(Dispatchers.IO) {
//            productDao.insertProduct(products)
//        }
//    }