package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ShoeSizeDao
import com.example.learncompose303.data.entities.ShoeSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShoeSizeRepository {
    suspend fun getShoeSizeById(id: Long): ShoeSize?
    fun getShoeSizesByLabel(labelId: Long): Flow<List<ShoeSize>>
    suspend fun insertShoeSize(shoeSize: ShoeSize): Long
    suspend fun insertShoeSizes(shoeSizes: List<ShoeSize>): List<Long>
    suspend fun updateShoeSize(shoeSize: ShoeSize)
    suspend fun deleteShoeSize(shoeSize: ShoeSize)
}

class ShoeSizeRepositoryImpl @Inject constructor(
    private val shoeSizeDao: ShoeSizeDao
) : ShoeSizeRepository {

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

    override suspend fun deleteShoeSize(shoeSize: ShoeSize) {
        withContext(Dispatchers.IO) {
            shoeSizeDao.deleteShoeSize(shoeSize)
        }
    }
}
