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
    suspend fun getOrderCount(): Flow<Int>
    suspend fun getOrderById(orderId: Int): Order?
    suspend fun getOrderByDate(startDate: Long): Flow<List<Order>>
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

    // Fetch count of all orders
    override suspend fun getOrderCount(): Flow<Int> {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderCount()
        }
    }

    // Get a specific order by ID
    override suspend fun getOrderById(orderId: Int): Order? {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderById(orderId)
        }
    }

    override suspend fun getOrderByDate(startDate: Long): Flow<List<Order>> {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderByDate(startDate)
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
