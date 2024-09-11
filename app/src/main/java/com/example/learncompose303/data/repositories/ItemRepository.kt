package com.example.learncompose303.data.repositories

import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.Item
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    fun getAllItems(): Flow<List<Item>> = itemDao.getAllItems()

    suspend fun insertItem(item: Item) = itemDao.insertItem(item)

    suspend fun updateItem(item: Item) = itemDao.updateItem(item)

    suspend fun deleteItem(item: Item) = itemDao.deleteItem(item)
}
