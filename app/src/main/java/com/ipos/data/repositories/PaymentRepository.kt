package com.ipos.data.repositories

import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.PaymentDao
import com.ipos.data.entities.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PaymentRepository {
    suspend fun getPaymentsForInvoice(invoiceId: Long): List<Payment>
    suspend fun insertPayment(payment: Payment): Long
}

class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
    private val deviceSession: DeviceSession
) : PaymentRepository {
    override suspend fun getPaymentsForInvoice(invoiceId: Long): List<Payment> {
        return withContext(Dispatchers.IO) {
            paymentDao.getPaymentsForInvoice(invoiceId)
        }
    }

    override suspend fun insertPayment(payment: Payment): Long {
        return withContext(Dispatchers.IO) {
            paymentDao.insertPayment(
                payment.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }
    }
}
