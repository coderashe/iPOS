package com.example.learncompose303.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.data.Item
import com.example.learncompose303.data.repositories.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ItemViewModel @Inject constructor(private val repository: ItemRepository) : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllItems().collect { listOfItems ->
                _items.value = listOfItems
            }
        }
    }

    fun insert(item: Item) = viewModelScope.launch {
        repository.insertItem(item)
    }

    fun update(item: Item) = viewModelScope.launch {
        repository.updateItem(item)
    }

    fun delete(item: Item) = viewModelScope.launch {
        repository.deleteItem(item)
    }
}
