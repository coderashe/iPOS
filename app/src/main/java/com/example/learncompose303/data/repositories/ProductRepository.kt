package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ProductRepository {
    suspend fun getProductById(id: Long): Product?
    suspend fun getAllProducts(): Flow<List<Product>>
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun insertProducts(products: List<Product>): List<Long> // Return list of inserted product IDs
    suspend fun getAllProductsOnce(): List<Product>
}

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getProductById(id: Long): Product? {
        return withContext(Dispatchers.IO) {
            productDao.getProductById(id)
        }
    }

    override suspend fun getAllProducts(): Flow<List<Product>> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProducts()
        }
    }

    override suspend fun insertProduct(product: Product): Long {
        return withContext(Dispatchers.IO) {
            productDao.insertProduct(product)
        }
    }

    override suspend fun updateProduct(product: Product) {
        return withContext(Dispatchers.IO) {
            productDao.updateProduct(product)
        }
    }

    override suspend fun deleteProduct(product: Product) {
        return withContext(Dispatchers.IO) {
            productDao.deleteProduct(product)
        }
    }

    override suspend fun insertProducts(products: List<Product>): List<Long> {
        return withContext(Dispatchers.IO) {
            productDao.insertProducts(products)
        }
    }

    override suspend fun getAllProductsOnce(): List<Product> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProductsOnce()
        }
    }

}