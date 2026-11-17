package com.ipos.data.repositories

import com.ipos.data.data_source.ShoeBrandDao
import com.ipos.data.entities.ShoeBrand
import com.ipos.data.entities.ShoeSize
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
    suspend fun findBrandByName(brandName: String): ShoeBrand?
    suspend fun findBrandByNameExact(brandName: String): ShoeBrand?
    suspend fun findBrandByNameCaseInsensitive(brandName: String): ShoeBrand?
}

class ShoeBrandRepositoryImpl @Inject constructor(
    private val shoeBrandDao: ShoeBrandDao
) : ShoeBrandRepository {

    override suspend fun getBrandNameById(brandId: Long): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getBrandNameById(brandId)
        }
    }

    override suspend fun findBrandByName(brandName: String): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getBrandByName(brandName)
        }
    }

    override suspend fun findBrandByNameExact(brandName: String): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getBrandByNameExact(brandName)
        }
    }

    override suspend fun findBrandByNameCaseInsensitive(brandName: String): ShoeBrand? {
        return withContext(Dispatchers.IO) {
            shoeBrandDao.getBrandByNameCaseInsensitive(brandName)
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
