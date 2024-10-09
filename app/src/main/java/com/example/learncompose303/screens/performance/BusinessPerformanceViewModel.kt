package com.example.learncompose303.screens.performance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.data.entities.HourPerformance
import com.example.learncompose303.data.entities.ProductPerformance
import com.example.learncompose303.data.repositories.BusinessRepository
import com.example.learncompose303.data.repositories.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessPerformanceViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    val totalExpenses: LiveData<Double> = businessRepository.getTotalExpenses().asLiveData()
    val totalSales: LiveData<Double> = businessRepository.getTotalSales().asLiveData()
    val topPerformingProducts: LiveData<List<ProductPerformance>> =
        businessRepository.getTopPerformingProducts().asLiveData()
    val peakHours: LiveData<List<HourPerformance>> = businessRepository.getPeakHours().asLiveData()
    val salesPerMonth: LiveData<Map<String, Double>> =
        businessRepository.getSalesPerMonth().asLiveData()

    // LiveData that holds sales per month as a map
    private val _salesPerMonthMap = MutableLiveData<Map<String, Double>>()
    val salesPerMonthMap: LiveData<Map<String, Double>> = _salesPerMonthMap

    init {
        viewModelScope.launch {
            invoiceRepository.getSalesPerMonth()
                .map { list -> list.associate { it.month to it.totalSales } }
                .collect {
                    _salesPerMonthMap.value = it
                }
        }
    }
}
