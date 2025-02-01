package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.CurrencyDao
import com.example.learncompose303.data.entities.Currency
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CurrencyRepository {
    suspend fun getCurrencyByCode(code: String): Currency?
    suspend fun insertCurrencies(currencies: List<Currency>)

}

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao
) : CurrencyRepository {
    fun getAllCurrencies(): Flow<List<Currency>> = currencyDao.getAllCurrencies()

    override suspend fun getCurrencyByCode(code: String): Currency? {
        return currencyDao.getCurrencyByCode(code)
    }

    override suspend fun insertCurrencies(currencies: List<Currency>) {
        currencyDao.insertCurrencies(currencies)
    }
}
