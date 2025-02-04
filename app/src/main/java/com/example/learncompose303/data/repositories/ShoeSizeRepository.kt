package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ShoeSizeDao
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ShoeSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShoeSizeRepository {
    suspend fun updateShoeSizeQuantity(shoeSizeId: Long, newQty: Long)
    suspend fun getShoeSizeById(id: Long): ShoeSize?
    fun getShoeSizesByLabel(labelId: Long): Flow<List<ShoeSize>>
    suspend fun insertShoeSize(shoeSize: ShoeSize): Long
    suspend fun insertShoeSizes(shoeSizes: List<ShoeSize>): List<Long>
    suspend fun updateShoeSize(shoeSize: ShoeSize)
    suspend fun deleteShoeSize(shoeSize: ShoeSize)
    fun getAllShoeSizes(): Flow<List<ShoeSize>>
    suspend fun getTotalQuantityByBrand(brandId: Long): Int
    suspend fun getAllShoeSizesOnce(): List<ShoeSize>
}

class ShoeSizeRepositoryImpl @Inject constructor(
    private val shoeSizeDao: ShoeSizeDao
) : ShoeSizeRepository {
    override suspend fun updateShoeSizeQuantity(shoeSizeId: Long, newQty: Long) {
        withContext(Dispatchers.IO) {
            shoeSizeDao.updateShoeSizeQuantity(shoeSizeId, newQty)
        }
    }

    override suspend fun insertShoeSizes(shoeSizes: List<ShoeSize>): List<Long> {
        return withContext(Dispatchers.IO) {
            shoeSizeDao.insertShoeSizes(shoeSizes)
        }
    }

    override suspend fun updateShoeSize(shoeSize: ShoeSize) {
        withContext(Dispatchers.IO) {
            shoeSizeDao.updateShoeSize(shoeSize)
        }
    }

    // Method to get all shoe sizes once (this should return a list of ShoeSize objects)
    override suspend fun getAllShoeSizesOnce(): List<ShoeSize> {
        return shoeSizeDao.getAllShoeSizesOnce()
    }

    override suspend fun getTotalQuantityByBrand(brandId: Long): Int {
        return shoeSizeDao.getTotalQuantityByBrand(brandId)
    }

    override fun getAllShoeSizes(): Flow<List<ShoeSize>> {
        return shoeSizeDao.getAllShoeSizes()
    }

    override suspend fun getShoeSizeById(id: Long): ShoeSize? {
        return withContext(Dispatchers.IO) {
            shoeSizeDao.getShoeSizeById(id)
        }
    }

    override fun getShoeSizesByLabel(labelId: Long): Flow<List<ShoeSize>> {
        return shoeSizeDao.getShoeSizesByLabel(labelId)
    }

    override suspend fun insertShoeSize(shoeSize: ShoeSize): Long {
        return withContext(Dispatchers.IO) {
            shoeSizeDao.insertShoeSize(shoeSize)
        }
    }

    override suspend fun deleteShoeSize(shoeSize: ShoeSize) {
        withContext(Dispatchers.IO) {
            shoeSizeDao.deleteShoeSize(shoeSize)
        }
    }
}
