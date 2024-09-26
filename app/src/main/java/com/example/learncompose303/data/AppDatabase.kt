package com.example.learncompose303.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.data_source.CartItemDao
import com.example.learncompose303.data.data_source.CustomerDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.InvoiceItemDao
import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.data_source.OrderDao
import com.example.learncompose303.data.data_source.PaymentDao
import com.example.learncompose303.data.data_source.PriceListDao
import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.data_source.StudentDao
import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Order
import com.example.learncompose303.data.entities.Payment
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Product

@Database(
    entities = [Item::class, Note::class,
        StudentEntity::class, Customer::class,
        Invoice::class, InvoiceItem::class,
        Payment::class, Product::class,
        CartItem::class, InvoiceLine::class,
        Order::class, PriceList::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun noteDao(): NoteDao
    abstract fun studentDao(): StudentDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun invoiceLineDao(): InvoiceLineDao
    abstract fun orderDao(): OrderDao
    abstract fun pricelistDao(): PriceListDao

    companion object {
        const val DATABASE_NAME = "ipos_db"
    }
}
