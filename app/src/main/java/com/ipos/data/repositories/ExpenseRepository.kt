package com.ipos.data.repositories

import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.ExpenseDao
import com.ipos.data.entities.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


interface ExpenseRepository {
    suspend fun insertExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenses(): Flow<Double?>
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteAllExpenses()
}

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val deviceSession: DeviceSession
) : ExpenseRepository {

    override suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.insertExpense(
                expense.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }
    }

    override fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    override fun getTotalExpenses(): Flow<Double?> = expenseDao.getTotalExpenses()

    override suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) { expenseDao.deleteExpense(expense) }
    }

    override suspend fun deleteAllExpenses() {
        withContext(Dispatchers.IO) { expenseDao.deleteAllExpenses() }
    }
}
