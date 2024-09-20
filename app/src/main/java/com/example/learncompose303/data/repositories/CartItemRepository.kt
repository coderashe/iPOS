package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.CartItemDao
import com.example.learncompose303.data.entities.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CartItemRepository {
    suspend fun insertCartItem(cartItem: CartItem): Long
    suspend fun getCartItemsForCustomer(customerId: Long): Flow<List<CartItem>>
    suspend fun updateCartItem(cartItem: CartItem)
    suspend fun deleteCartItem(cartItem: CartItem)
    suspend fun clearCartForCustomer(customerId: Long)
}

class CartItemRepositoryImpl @Inject constructor(
    private val cartItemDao: CartItemDao
) : CartItemRepository {

    override suspend fun insertCartItem(cartItem: CartItem): Long {
        return withContext(Dispatchers.IO) {
            cartItemDao.insertCartItem(cartItem)
        }
    }

    override suspend fun getCartItemsForCustomer(customerId: Long): Flow<List<CartItem>> {
        return withContext(Dispatchers.IO) {
            cartItemDao.getCartItemsForCustomer(customerId)
        }
    }

    override suspend fun updateCartItem(cartItem: CartItem) {
        return withContext(Dispatchers.IO) {
            cartItemDao.updateCartItem(cartItem)
        }
    }

    override suspend fun deleteCartItem(cartItem: CartItem) {
        return withContext(Dispatchers.IO) {
            cartItemDao.deleteCartItem(cartItem)
        }
    }

    override suspend fun clearCartForCustomer(customerId: Long) {
        return withContext(Dispatchers.IO) {
            cartItemDao.clearCartForCustomer(customerId)
        }
    }
}
