package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.learncompose303.data.entities.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<Currency>)

    @Query("SELECT * FROM currencies")
    fun getAllCurrencies(): Flow<List<Currency>>

    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    suspend fun getCurrencyByCode(code: String): Currency?

    @Query("SELECT rateToBase FROM currencies WHERE code = :code LIMIT 1")
    suspend fun getExchangeRate(code: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: Currency)

    @Query("SELECT * FROM currencies")
    suspend fun getAllCurrenciesOnce(): List<Currency>

    @Query("SELECT * FROM currencies WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<Currency>

    @Query("UPDATE currencies SET isSynced = 1 WHERE code IN (:codes)")
    suspend fun markSynced(codes: List<String>)

    @Query("SELECT COUNT(*) FROM currencies WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
