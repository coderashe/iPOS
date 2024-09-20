package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.PaymentDao
import com.example.learncompose303.data.entities.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PaymentRepository {
    suspend fun getPaymentsForInvoice(invoiceId: Long): List<Payment>
    suspend fun insertPayment(payment: Payment): Long
}

class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao
) : PaymentRepository {
    override suspend fun getPaymentsForInvoice(invoiceId: Long): List<Payment> {
        return withContext(Dispatchers.IO) {
            paymentDao.getPaymentsForInvoice(invoiceId)
        }
    }

    override suspend fun insertPayment(payment: Payment): Long {
        return withContext(Dispatchers.IO) {
            paymentDao.insertPayment(payment)
        }
    }

}
