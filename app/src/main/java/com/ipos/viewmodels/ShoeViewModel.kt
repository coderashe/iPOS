package com.ipos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipos.data.entities.ShoeBrand
import com.ipos.data.entities.ShoeSize
import com.ipos.data.repositories.ShoeBrandRepository
import com.ipos.data.repositories.ShoeSizeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoeViewModel @Inject constructor(
    private val shoeBrandRepository: ShoeBrandRepository,
    private val shoeSizeRepository: ShoeSizeRepository
) : ViewModel() {

    private val _shoeBrands = MutableStateFlow<List<ShoeBrand>>(emptyList())
    val shoeBrands: StateFlow<List<ShoeBrand>> = _shoeBrands.asStateFlow()

    private val _shoeSizes = MutableStateFlow<List<ShoeSize>>(emptyList())
    val shoeSizes: StateFlow<List<ShoeSize>> = _shoeSizes.asStateFlow()

    init {
        fetchShoeBrands()
    }

    private fun fetchShoeBrands() {
        viewModelScope.launch {
            shoeBrandRepository.getAllShoeBrands().collectLatest { _shoeBrands.value = it }
        }
    }

    fun insertShoeBrand(brandName: String) {
        viewModelScope.launch {
            val brand = ShoeBrand(brandName = brandName)
            shoeBrandRepository.insertShoeBrand(brand)
            fetchShoeBrands() // Refresh list
        }
    }

    fun insertShoeSize(brandID: Long, size: Double, quantity: Long) {
        viewModelScope.launch {
            val shoeSize = ShoeSize(brandID = brandID, size = size, quantity = quantity)
            shoeSizeRepository.insertShoeSizes(listOf(shoeSize))
        }
    }
}
