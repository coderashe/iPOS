package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ExpenseDao
import com.example.learncompose303.data.entities.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


interface ExpenseRepository {
    suspend fun insertExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenses(): Flow<Double>
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteAllExpenses()
}

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.insertExpense(expense)
        }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
    }

    override fun getTotalExpenses(): Flow<Double> {
        return expenseDao.getTotalExpenses()
    }

    override suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.deleteExpense(expense)
        }
    }

    override suspend fun deleteAllExpenses() {
        withContext(Dispatchers.IO) {
            expenseDao.deleteAllExpenses()
        }
    }
}
