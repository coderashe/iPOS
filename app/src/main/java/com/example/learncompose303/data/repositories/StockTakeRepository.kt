package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ShoeBrandDao
import com.example.learncompose303.data.data_source.StockTakeDao
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import com.example.learncompose303.data.entities.StockTake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface StockTakeRepository {
    fun getAllStockTakes(): Flow<List<StockTake>>
    suspend fun insertStockTake(stockTake: StockTake)
}

class StockTakeRepositoryImpl @Inject constructor(
    private val stockTakeDao: StockTakeDao
) : StockTakeRepository {

    override fun getAllStockTakes(): Flow<List<StockTake>> {
        return stockTakeDao.getAllStockTakes()
    }

    override suspend fun insertStockTake(stockTake: StockTake) {
        return withContext(Dispatchers.IO) {
            stockTakeDao.insertStockTake(stockTake)
        }
    }


}
