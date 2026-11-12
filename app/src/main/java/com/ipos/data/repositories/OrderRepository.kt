package com.ipos.data.repositories

import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.OrderDao
import com.ipos.data.entities.Order
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
    private val orderDao: OrderDao,
    private val deviceSession: DeviceSession
) : OrderRepository {

    override suspend fun insertOrder(order: Order) =
        withContext(Dispatchers.IO) {
            orderDao.insertOrder(
                order.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }

    override suspend fun getAllOrders(): Flow<List<Order>> =
        withContext(Dispatchers.IO) { orderDao.getAllOrders() }

    override suspend fun getOrderCount(): Flow<Int> =
        withContext(Dispatchers.IO) { orderDao.getOrderCount() }

    override suspend fun getOrderById(orderId: Int): Order? =
        withContext(Dispatchers.IO) { orderDao.getOrderById(orderId) }

    override suspend fun getOrderByDate(startDate: Long): Flow<List<Order>> =
        withContext(Dispatchers.IO) { orderDao.getOrderByDate(startDate) }

    override suspend fun deleteOrder(order: Order) =
        withContext(Dispatchers.IO) { orderDao.deleteOrder(order) }

    override suspend fun deleteAllOrders() =
        withContext(Dispatchers.IO) { orderDao.deleteAllOrders() }
}
