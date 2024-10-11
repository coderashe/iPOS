package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.PriceListDao
import com.example.learncompose303.data.entities.PriceList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PriceListRepository {
    suspend fun getPriceListById(id: Long): PriceList?
    suspend fun getPriceListByProductId(productId: Long): PriceList?
    fun getAllPriceLists(): Flow<List<PriceList>>
    suspend fun insertPriceListsAll(priceLists: List<PriceList>): List<Long>

    suspend fun insertPriceList(priceList: PriceList): Long
    suspend fun insertPriceLists(priceLists: List<PriceList>)
    suspend fun updatePriceList(priceList: PriceList)
    suspend fun deletePriceList(priceList: PriceList)
    suspend fun insertCurrentPriceList(
        price: Double,
        productDescription: String,
        productId: Long
    ): Int
}

class PriceListRepositoryImpl @Inject constructor(
    private val priceListDao: PriceListDao
) : PriceListRepository {

    override suspend fun getPriceListById(id: Long): PriceList? {
        return withContext(Dispatchers.IO) {
            priceListDao.getPriceListById(id)
        }
    }

    override suspend fun getPriceListByProductId(productId: Long): PriceList? {
        return withContext(Dispatchers.IO) {
            priceListDao.getPriceListByProductId(productId)
        }
    }

    override suspend fun insertCurrentPriceList(
        price: Double,
        productDescription: String,
        productId: Long
    ): Int {
        return withContext(Dispatchers.IO) {
            priceListDao.insertCurrentPriceList(price, productDescription, productId)
        }
    }

    override fun getAllPriceLists(): Flow<List<PriceList>> {
        return priceListDao.getAllPriceLists()
    }

    override suspend fun insertPriceListsAll(priceLists: List<PriceList>): List<Long> {
        return withContext(Dispatchers.IO) {
            priceLists.map { priceListDao.insertPriceList(it) }
        }
    }

    override suspend fun insertPriceList(priceList: PriceList): Long {
        return withContext(Dispatchers.IO) {
            priceListDao.insertPriceList(priceList)
        }
    }

    override suspend fun insertPriceLists(priceLists: List<PriceList>) {
        return withContext(Dispatchers.IO) {
            priceListDao.insertPriceLists(priceLists)
        }
    }

    override suspend fun updatePriceList(priceList: PriceList) {
        return withContext(Dispatchers.IO) {
            priceListDao.updatePriceList(priceList)
        }
    }

    override suspend fun deletePriceList(priceList: PriceList) {
        return withContext(Dispatchers.IO) {
            priceListDao.deletePriceList(priceList)
        }
    }
}
