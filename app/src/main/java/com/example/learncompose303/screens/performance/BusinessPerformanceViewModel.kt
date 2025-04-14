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

    val totalExpenses: LiveData<Double> = businessRepository.getTotalExpenses()
        .map { it ?: 0.0 }  // Replace null with 0.0 or another default value
        .asLiveData()

    val totalSalesInUsd: LiveData<Double> = businessRepository.getTotalSalesInUsd().asLiveData()
    val totalSales: LiveData<Double> = businessRepository.getTotalSales().asLiveData()
    val topPerformingProducts: LiveData<List<ProductPerformance>> =
        businessRepository.getTopPerformingProducts().asLiveData()
    val peakHours: LiveData<List<HourPerformance>> = businessRepository.getPeakHours().asLiveData()

    // LiveData that holds sales per month as a map
    val salesPerMonth: LiveData<Map<String, Double>> =
        MutableLiveData<Map<String, Double>>().apply {
            viewModelScope.launch {
                invoiceRepository.getSalesPerMonth()
                    .map { list ->
                        list.associate {
                            it.monthName to it.totalSales
                        }
                    }
                    .collect {
                        value = it  // Update the LiveData value directly
                    }
            }
        }

    // LiveData that holds sales per month as a map
//    val salesPerMonth: LiveData<Map<String, Double>> =
//        MutableLiveData<Map<String, Double>>().apply {
//            viewModelScope.launch {
//                invoiceRepository.getSalesPerMonth()
//                    .map { list ->
//                        list.associate {
//                            // Map month number to month name
//                            it.monthName to it.totalSales
//                        }
//                    }
//                    .collect {
//                        value = it // Update the LiveData value directly
//                    }
//            }
//        }
}