package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.learncompose303.data.entities.CashFlow

@Dao
interface CashFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashFlow(cashFlow: CashFlow)

    @Update
    suspend fun updateCashFlow(cashFlow: CashFlow)

    @Delete
    suspend fun deleteCashFlow(cashFlow: CashFlow)

    @Query("SELECT * FROM cashflow WHERE cashFlowId = :id")
    suspend fun getCashFlowById(id: Long): CashFlow?

    @Query("SELECT * FROM cashflow ORDER BY transactionDate DESC")
    fun getAllCashFlows(): Flow<List<CashFlow>>
}
