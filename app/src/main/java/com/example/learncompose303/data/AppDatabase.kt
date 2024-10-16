package com.example.learncompose303.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.data_source.CartItemDao
import com.example.learncompose303.data.data_source.CustomerDao
import com.example.learncompose303.data.data_source.DayReportsDao
import com.example.learncompose303.data.data_source.ExpenseDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.InvoiceItemDao
import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.data_source.OrderDao
import com.example.learncompose303.data.data_source.PaymentDao
import com.example.learncompose303.data.data_source.PriceListDao
import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.data_source.ProductDayReportDao
import com.example.learncompose303.data.data_source.StudentDao
import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.DayReports
import com.example.learncompose303.data.entities.Expense
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Order
import com.example.learncompose303.data.entities.Payment
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.data.entities.ProductDayReport
import com.example.learncompose303.data.entities.User

@Database(
    entities = [Item::class, Note::class,
        StudentEntity::class, Customer::class,
        Invoice::class, InvoiceItem::class, InvoiceLine::class,
        Payment::class, Product::class,
        CartItem::class,
        Order::class, PriceList::class,
        ProductDayReport::class, DayReports::class,
        User::class,
        Expense::class],
    version = 34,
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
    abstract fun productDayReportDao(): ProductDayReportDao
    abstract fun dayReportsDao(): DayReportsDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "ipos_db"
    }
}
