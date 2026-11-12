package com.ipos.data.repositories

import com.ipos.data.data_source.ShoeSizeDao
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShoeSizeRepository {
    suspend fun getShoeSizeById(id: Long): ShoeSize?
    suspend fun updateShoeSizeQuantity(shoeSizeId: Long, newQty: Long)
    fun getShoeSizesByLabel(labelId: Long): Flow<List<ShoeSize>>
    suspend fun insertShoeSize(shoeSize: ShoeSize): Long
    suspend fun insertShoeSizes(shoeSizes: List<ShoeSize>): List<Long>
    suspend fun updateShoeSize(shoeSize: ShoeSize)
    suspend fun deleteShoeSize(shoeSize: ShoeSize)
    fun getAllShoeSizes(): Flow<List<ShoeSize>>
    suspend fun getTotalQuantityByBrand(brandId: Long): Int
    suspend fun getAllShoeSizesOnce(): List<ShoeSize>
    suspend fun findSizeByBrandAndValue(brandId: Long, size: Double): ShoeSize?
    suspend fun findSizeByValue(size: Double): ShoeSize?
}

class ShoeSizeRepositoryImpl @Inject constructor(
    private val shoeSizeDao: ShoeSizeDao
) : ShoeSizeRepository {

    override suspend fun findSizeByValue(size: Double): ShoeSize? {
        return withContext(Dispatchers.IO) {
            shoeSizeDao.getSizeByValue(size)
        }
    }

    override suspend fun getShoeSizeById(id: Long): ShoeSize? {
        return withContext(Dispatchers.IO) {
            shoeSizeDao.getShoeSizeById(id)
        }
    }

    override suspend fun findSizeByBrandAndValue(brandId: Long, size: Double): ShoeSize? {
        return withContext(Dispatchers.IO) {
            shoeSizeDao.getSizeByBrandAndValue(brandId, size)
        }
    }

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
