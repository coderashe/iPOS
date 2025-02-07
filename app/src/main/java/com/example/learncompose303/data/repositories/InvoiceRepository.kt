package com.example.learncompose303.data.repositories

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import com.example.learncompose303.data.data_source.CurrencyDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.entities.Currency
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.MonthlySales
import com.example.learncompose303.data.entities.mapInvoiceToFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface InvoiceRepository {
    suspend fun insertInvoice(invoice: Invoice): Long
    suspend fun getInvoiceById(id: Long): Invoice?
    suspend fun getAllInvoices(): Flow<List<Invoice>>
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun deleteInvoice(invoice: Invoice)
    suspend fun getInvoicesByDate(startDate: Long): Flow<List<Invoice>>
    suspend fun getSalesPerMonth(): Flow<List<MonthlySales>>
    suspend fun getExchangeRate(currencyCode: String): Double
    suspend fun getAllCurrencies(): Flow<List<Currency>>
    suspend fun insertCurrencies(currencies: List<Currency>)
    suspend fun insertCurrency(currency: Currency)
    suspend fun getAllCurrenciesOnce(): List<Currency>
    suspend fun getTotalSalesPerCurrency(): Flow<Map<String, Double>>


}

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val currencyDao: CurrencyDao
) : InvoiceRepository {

    override suspend fun getTotalSalesPerCurrency(): Flow<Map<String, Double>> {
        return getAllInvoices().map { invoices ->
            invoices.groupBy { it.currencyCode }.mapValues { entry ->
                entry.value.sumOf { it.totalAmount }
            }
        }
    }

    override suspend fun insertCurrency(currency: Currency) {
        currencyDao.insertCurrency(currency)
    }

    override suspend fun getAllCurrenciesOnce(): List<Currency> {
        return withContext(Dispatchers.IO) {
            currencyDao.getAllCurrenciesOnce()
        }
    }

    override suspend fun insertCurrencies(currencies: List<Currency>) {
        currencyDao.insertCurrencies(currencies)
    }

    override suspend fun getAllCurrencies(): Flow<List<Currency>> {
        return withContext(Dispatchers.IO) {
            currencyDao.getAllCurrencies()
        }
    }

    override suspend fun getExchangeRate(currencyCode: String): Double {
        return currencyDao.getExchangeRate(currencyCode) ?: 1.0 // Default to USD (1.0)
    }

    override suspend fun insertInvoice(invoice: Invoice): Long {
        return withContext(Dispatchers.IO) {
            invoiceDao.insertInvoice(invoice)
        }
    }

    override suspend fun getSalesPerMonth(): Flow<List<MonthlySales>> {
        return withContext(Dispatchers.IO) {
            invoiceDao.getSalesPerMonth()
        }
    }

    override suspend fun updateInvoice(invoice: Invoice) {
        return withContext(Dispatchers.IO) {
            invoiceDao.updateInvoice(invoice)
        }
    }

    override suspend fun getInvoicesByDate(startDate: Long): Flow<List<Invoice>> {
        return withContext(Dispatchers.IO) {
            invoiceDao.getInvoicesByDate(startDate)
        }
    }

    override suspend fun deleteInvoice(invoice: Invoice) {
        return withContext(Dispatchers.IO) {
            invoiceDao.deleteInvoice(invoice)
        }
    }

    override suspend fun getInvoiceById(id: Long): Invoice? {
        return withContext(Dispatchers.IO) {
            invoiceDao.getInvoiceById(id)
        }
    }

    override suspend fun getAllInvoices(): Flow<List<Invoice>> {
        return withContext(Dispatchers.IO) {
            invoiceDao.getAllInvoices()
        }
    }

}

//    for FireStore
//    suspend fun syncInvoiceWithFirestore(invoice: Invoice)

//    private val firestore = FirebaseFirestore.getInstance()
//    override suspend fun insertInvoice(invoice: Invoice): Long {
//        return withContext(Dispatchers.IO) {
//            val id = invoiceDao.insertInvoice(invoice)
//            // Sync with Firestore
//            syncInvoiceWithFirestore(invoice)
//            id
//        }
//    }
//
//    override suspend fun updateInvoice(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.updateInvoice(invoice)
//            // Sync updated invoice with Firestore
//            syncInvoiceWithFirestore(invoice)
//        }
//    }
//
//    override suspend fun deleteInvoice(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.deleteInvoice(invoice)
//            // Remove from Firestore
//            firestore.collection("invoices").document(invoice.invoiceId.toString())
//                .delete()
//        }
//    }
//    override suspend fun syncInvoiceWithFirestore(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            val invoiceData = mapInvoiceToFirestore(invoice)
//            firestore.collection("invoices")
//                .document(invoice.invoiceId.toString())
//                .set(invoiceData)
//                .addOnSuccessListener {
//                    Log.d("FirestoreSync", "Invoice synced successfully")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("FirestoreSync", "Failed to sync invoice", e)
//                }
//        }
//    }
