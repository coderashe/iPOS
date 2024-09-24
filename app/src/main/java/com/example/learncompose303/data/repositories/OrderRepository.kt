package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.OrderDao
import com.example.learncompose303.data.entities.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OrderRepository {
    suspend fun insertOrder(order: Order)
    suspend fun getAllOrders(): Flow<List<Order>>
    suspend fun getOrderById(orderId: Int): Order?
    suspend fun deleteOrder(order: Order)
    suspend fun deleteAllOrders()
}

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    // Insert a new order
    override suspend fun insertOrder(order: Order) {
        return withContext(Dispatchers.IO) {
            orderDao.insertOrder(order)
        }
    }

    // Fetch all orders
    override suspend fun getAllOrders(): Flow<List<Order>> {
        return withContext(Dispatchers.IO) {
            orderDao.getAllOrders()
        }
    }

    // Get a specific order by ID
    override suspend fun getOrderById(orderId: Int): Order? {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderById(orderId)
        }
    }

    // Delete a specific order
    override suspend fun deleteOrder(order: Order) {
        return withContext(Dispatchers.IO) {
            orderDao.deleteOrder(order)
        }
    }

    // Clear all orders
    override suspend fun deleteAllOrders() {
        return withContext(Dispatchers.IO) {
            orderDao.deleteAllOrders()
        }
    }
}
