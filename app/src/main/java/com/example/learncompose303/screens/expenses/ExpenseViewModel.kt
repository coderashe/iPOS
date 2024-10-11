package com.example.learncompose303.screens.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.data.entities.Expense
import com.example.learncompose303.data.repositories.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val allExpenses: LiveData<List<Expense>> = expenseRepository.getAllExpenses().asLiveData()

    //    val totalExpenses: LiveData<Double> = expenseRepository.getTotalExpenses().asLiveData()
    val totalExpenses: LiveData<Double> = expenseRepository.getTotalExpenses().asLiveData().map {
        it ?: 0.0 // Provide a default value if null
    }

    fun addExpense(description: String, amount: Double, expenseDate: Long, category: String) {
        val expense = Expense(
            description = description,
            amount = amount,
            expenseDate = expenseDate,
            category = category
        )
        viewModelScope.launch {
            expenseRepository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun deleteAllExpenses() {
        viewModelScope.launch {
            expenseRepository.deleteAllExpenses()
        }
    }
}
