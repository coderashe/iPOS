package com.ipos.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ipos.data.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expenses WHERE isSynced = 0 AND storeId != '' AND storeId IS NOT NULL LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<Expense>

    @Query("UPDATE expenses SET isSynced = 1 WHERE expenseId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM expenses WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE expenses SET deviceId = :deviceId, storeId = :storeId WHERE deviceId = '' OR deviceId IS NULL")
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String)
}
