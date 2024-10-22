package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.entities.Inventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ProductRepository {
    suspend fun getProductById(id: Long): Inventory?
    suspend fun getAllProducts(): Flow<List<Inventory>>
    suspend fun insertProduct(inventory: Inventory): Long
    suspend fun updateProduct(inventory: Inventory)
    suspend fun deleteProduct(inventory: Inventory)
    suspend fun insertProducts(inventories: List<Inventory>): List<Long> // Return list of inserted product IDs
    suspend fun getAllProductsOnce(): List<Inventory>
}

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getProductById(id: Long): Inventory? {
        return withContext(Dispatchers.IO) {
            productDao.getProductById(id)
        }
    }

    override suspend fun getAllProducts(): Flow<List<Inventory>> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProducts()
        }
    }

    override suspend fun insertProduct(inventory: Inventory): Long {
        return withContext(Dispatchers.IO) {
            productDao.insertProduct(inventory)
        }
    }

    override suspend fun updateProduct(inventory: Inventory) {
        return withContext(Dispatchers.IO) {
            productDao.updateProduct(inventory)
        }
    }

    override suspend fun deleteProduct(inventory: Inventory) {
        return withContext(Dispatchers.IO) {
            productDao.deleteProduct(inventory)
        }
    }

    override suspend fun insertProducts(inventories: List<Inventory>): List<Long> {
        return withContext(Dispatchers.IO) {
            productDao.insertProducts(inventories)
        }
    }

    override suspend fun getAllProductsOnce(): List<Inventory> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProductsOnce()
        }
    }

}
