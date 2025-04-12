package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ShoeBrandDao
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShoeBrandRepository {
    suspend fun getShoeBrandById(id: Long): ShoeBrand?
    suspend fun insertShoeBrand(shoeBrand: ShoeBrand): Long
    suspend fun insertShoeBrands(shoeBrands: List<ShoeBrand>): List<Long>
    suspend fun updateShoeBrand(shoeBrand: ShoeBrand)
    suspend fun deleteShoeBrand(shoeBrand: ShoeBrand)
    suspend fun getAllShoeBrands(): Flow<List<ShoeBrand>>
    suspend fun getAllShoeBrandsOnce(): List<ShoeBrand>
    suspend fun getBrandNameById(brandId: Long): ShoeBrand?
    fun getBrandById(brandId: Long): Flow<ShoeBrand?>
}

class ShoeBrandRepositoryImpl @Inject constructor(
    private val shoeBrandDao: ShoeBrandDao
) : ShoeBrandRepository {

    override suspend fun getBrandNameById(brandId: Long): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getBrandNameById(brandId)
        }
    }


    override suspend fun getAllShoeBrands(): Flow<List<ShoeBrand>> {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getAllShoeBrands()
        }
    }

    // Method to get all shoe brands once
    override suspend fun getAllShoeBrandsOnce(): List<ShoeBrand> {
        return shoeBrandDao.getAllShoeBrandsOnce()
    }

    override suspend fun getShoeBrandById(id: Long): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getShoeBrandById(id)
        }
    }

    override fun getBrandById(brandId: Long): Flow<ShoeBrand?> {
        return shoeBrandDao.getBrandById(brandId)
    }

    override suspend fun insertShoeBrand(shoeBrand: ShoeBrand): Long {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.insertShoeBrand(shoeBrand)
        }
    }

    override suspend fun insertShoeBrands(shoeBrands: List<ShoeBrand>): List<Long> {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.insertShoeBrands(shoeBrands)
        }
    }

    override suspend fun updateShoeBrand(shoeBrand: ShoeBrand) {
        withContext(Dispatchers.IO) {
            shoeBrandDao.updateShoeBrand(shoeBrand)
        }
    }

    override suspend fun deleteShoeBrand(shoeBrand: ShoeBrand) {
        withContext(Dispatchers.IO) {
            shoeBrandDao.deleteShoeBrand(shoeBrand)
        }
    }
}
