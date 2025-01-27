package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ShoeBrandDao
import com.example.learncompose303.data.entities.ShoeBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShoeBrandRepository {
    suspend fun getLabelById(id: Long): ShoeBrand?
    fun getAllLabels(): Flow<List<ShoeBrand>>
    suspend fun insertLabel(shoeBrand: ShoeBrand): Long
    suspend fun insertLabels(shoeBrands: List<ShoeBrand>): List<Long>
    suspend fun updateLabel(shoeBrand: ShoeBrand)
    suspend fun deleteLabel(shoeBrand: ShoeBrand)
}

class ShoeBrandRepositoryImpl @Inject constructor(
    private val shoeBrandDao: ShoeBrandDao
) : ShoeBrandRepository {

    override suspend fun getLabelById(id: Long): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getLabelById(id)
        }
    }

    override fun getAllLabels(): Flow<List<ShoeBrand>> {
        return shoeBrandDao.getAllLabels()
    }

    override suspend fun insertLabel(shoeBrand: ShoeBrand): Long {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.insertLabel(shoeBrand)
        }
    }

    override suspend fun insertLabels(shoeBrands: List<ShoeBrand>): List<Long> {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.insertLabels(shoeBrands)
        }
    }

    override suspend fun updateLabel(shoeBrand: ShoeBrand) {
        withContext(Dispatchers.IO) {
            shoeBrandDao.updateLabel(shoeBrand)
        }
    }

    override suspend fun deleteLabel(shoeBrand: ShoeBrand) {
        withContext(Dispatchers.IO) {
            shoeBrandDao.deleteLabel(shoeBrand)
        }
    }
}
